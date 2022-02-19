package org.gravidence.gravifon.playlist.manage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ConfigUtil.configHomeDir
import org.gravidence.gravifon.configuration.FileStorage
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.playback.SubPlaybackStartEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayCurrentEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayNextEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayPrevEvent
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
            is SubPlaylistPlayCurrentEvent -> playCurrent(event.playlist, event.playlistItem)
            is SubPlaylistPlayNextEvent -> playNext(event.playlist)
            is SubPlaylistPlayPrevEvent -> playPrev(event.playlist)
        }
    }

    private fun play(playlist: Playlist, trackPlaylistItem: TrackPlaylistItem?): TrackPlaylistItem? {
        if (trackPlaylistItem != null) {
            GravifonContext.activePlaylist.value = playlist
            publish(SubPlaybackStartEvent(trackPlaylistItem.track))
        }

        return trackPlaylistItem
    }

    @Synchronized
    fun resolveNext(playlist: Playlist): TrackPlaylistItem? {
        return priorityPlaylist?.moveToNextTrack() ?: playlist.moveToNextTrack()
    }

    @Synchronized
    fun playCurrent(playlist: Playlist, playlistItem: PlaylistItem?): TrackPlaylistItem? {
        val currentTrack = if (playlistItem != null) {
            playlist.moveToSpecific(playlistItem)
            playlist.peekCurrentTrack()
        } else {
            priorityPlaylist?.moveToCurrentTrack() ?: playlist.moveToCurrentTrack()
        }

        return play(playlist, currentTrack)
    }

    @Synchronized
    fun playNext(playlist: Playlist): TrackPlaylistItem? {
        return play(playlist, resolveNext(playlist))
    }

    @Synchronized
    fun playPrev(playlist: Playlist): TrackPlaylistItem? {
        return play(playlist, priorityPlaylist?.moveToPrevTrack() ?: playlist.moveToPrevTrack())
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