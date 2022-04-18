package org.gravidence.gravifon.playlist.manage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ConfigUtil.configHomeDir
import org.gravidence.gravifon.configuration.FileStorage
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.playback.StartPlaybackEvent
import org.gravidence.gravifon.event.playback.StopPlaybackAfterEvent
import org.gravidence.gravifon.event.playback.StopPlaybackEvent
import org.gravidence.gravifon.event.playlist.*
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.Stateful
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.streams.toList

private val logger = KotlinLogging.logger {}

@Component
class PlaylistManager : EventAware, Stateful {

    private val regularPlaylists: MutableList<Playlist> = mutableListOf()
    // TODO think how to make it immutable
    private var priorityPlaylist: Playlist? = null

    override val fileStorage: FileStorage = PlaylistManagerFileStorage()

    init {
        fileStorage.read()
    }

    override fun consume(event: Event) {
        when (event) {
            is PlayCurrentFromPlaylistEvent -> playCurrent(event.playlist, event.playlistItem)
            is PlayNextFromPlaylistEvent -> playNext(event.playlist)
            is PlayPreviousFromPlaylistEvent -> playPrev(event.playlist)
            is RemoveFromPlaylistEvent -> event.apply {
                // only handle users of PlaylistManager, other playlist holders are likely have own update logic
                if (getPlaylist(playlist.id()) != null) {
                    playlist.remove(playlistItemIndexes.map { it + 1 }.toSet())
                    publish(PlaylistUpdatedEvent(playlist))
                }
            }
            is StopPlaybackAfterEvent -> stopAfter(event.n)
        }
    }

    private fun play(playlist: Playlist, trackPlaylistItem: TrackPlaylistItem?): TrackPlaylistItem? {
        if (trackPlaylistItem == null) {
            // forcing a playback action when there's no element to play,
            // that effectively means stopping current activity (if any) and do nothing further
            publish(StopPlaybackEvent())
        } else {
            GravifonContext.activePlaylist.value = playlist
            publish(StartPlaybackEvent(trackPlaylistItem.track))
        }

        return trackPlaylistItem
    }

    private fun applyStopAfter(trackPlaylistItem: TrackPlaylistItem?): TrackPlaylistItem? {
        return trackPlaylistItem?.let {
            return (if (GravifonContext.stopAfterState.value.stop) {
                null
            } else {
                it
            }).also {
                GravifonContext.stopAfterState.value = GravifonContext.stopAfterState.value.decreaseAndGet()
            }
        }
    }

    @Synchronized
    fun resolveCurrent(playlist: Playlist, playlistItem: PlaylistItem? = null): TrackPlaylistItem? {
        val currentTrack = if (playlistItem != null) {
            playlist.moveToSpecific(playlistItem)
            playlist.peekCurrentTrack()
        } else {
            priorityPlaylist?.moveToCurrentTrack() ?: playlist.moveToCurrentTrack()
        }

        return applyStopAfter(currentTrack)
    }

    @Synchronized
    fun playCurrent(playlist: Playlist, playlistItem: PlaylistItem? = null): TrackPlaylistItem? {
        return play(playlist, resolveCurrent(playlist, playlistItem))
    }

    @Synchronized
    fun resolveNext(playlist: Playlist): TrackPlaylistItem? {
        return applyStopAfter(priorityPlaylist?.moveToNextTrack() ?: playlist.moveToNextTrack())
    }

    @Synchronized
    fun playNext(playlist: Playlist): TrackPlaylistItem? {
        return play(playlist, resolveNext(playlist))
    }

    @Synchronized
    fun resolvePrev(playlist: Playlist): TrackPlaylistItem? {
        return applyStopAfter(priorityPlaylist?.moveToPrevTrack() ?: playlist.moveToPrevTrack())
    }

    @Synchronized
    fun playPrev(playlist: Playlist): TrackPlaylistItem? {
        return play(playlist, resolvePrev(playlist))
    }

    @Synchronized
    fun getPlaylist(playlistId: String, holder: List<Playlist>): Playlist? {
        return holder.firstOrNull { it.id() == playlistId }
    }

    @Synchronized
    fun getPlaylist(playlistId: String): Playlist? {
        return getPlaylist(playlistId, regularPlaylists) ?: priorityPlaylist?.takeIf { playlistId == it.id() }
    }

    @Synchronized
    fun addPlaylist(playlist: Playlist) {
        when (playlist) {
            is Queue -> priorityPlaylist = playlist
            else -> addPlaylist(playlist, regularPlaylists)
        }
    }

    @Synchronized
    private fun addPlaylist(playlist: Playlist, holder: MutableList<Playlist>) {
        if (getPlaylist(playlist.id(), holder) == null) {
            holder += playlist
        }
    }

    fun stopAfter(n: Int) {
        GravifonContext.stopAfterState.value = GravifonContext.stopAfterState.value.setAndGet(n.coerceAtLeast(0))
    }

    inner class PlaylistManagerFileStorage : FileStorage(storageDir = configHomeDir.resolve("playlist")) {

        override fun read() {
            val playlistConfigFiles = try {
                Files.list(storageDir).toList()
            } catch (e: Exception) {
                listOf()
            }.also {
                logger.info { "Discovered playlist files: $it" }
            }

            playlistConfigFiles.map { playlistConfigFile ->
                logger.debug { "Read playlist from $playlistConfigFile" }

                try {
                    addPlaylist(gravifonSerializer.decodeFromString(Files.readString(playlistConfigFile))).also {
                        logger.trace { "Playlist loaded: $it" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to read playlist from $playlistConfigFile" }
                }
            }
        }

        override fun write() {
            regularPlaylists.forEach { writePlaylist(it) }
            priorityPlaylist?.let { writePlaylist(it) }
        }

        private fun writePlaylist(playlist: Playlist) {
            val playlistId = playlist.id()
            val playlistFile = Path.of(storageDir.toString(), playlistId).also {
                logger.debug { "Write playlist to $it" }
            }
            try {
                val playlistAsString = gravifonSerializer.encodeToString(playlist).also {
                    logger.trace { "Playlist ($playlistId) to be persisted: $it" }
                }
                Files.writeString(
                    playlistFile,
                    playlistAsString,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to write playlist to $playlistFile" }
            }
        }

    }

}