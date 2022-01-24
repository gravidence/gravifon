package org.gravidence.gravifon.playlist.manage

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ConfigUtil.configHomeDir
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventHandler
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.playback.SubPlaybackStartEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistActivatePriorityPlaylistEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistActivateRegularPlaylistEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayNextEvent
import org.gravidence.gravifon.orchestration.OrchestratorConsumer
import org.gravidence.gravifon.orchestration.PlaylistManagerConsumer
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.createDirectories
import kotlin.streams.toList

private val logger = KotlinLogging.logger {}

@Component
class PlaylistManager(val consumers: List<PlaylistManagerConsumer>) : EventHandler(), OrchestratorConsumer {

    private val configuration = Configuration()

    private val regularPlaylists: MutableList<Playlist> = mutableListOf()
    private var activeRegularPlaylistId: UUID? = null
    private var currentRegularTrackPlaylistItem: TrackPlaylistItem? = null
    private val priorityPlaylists: MutableList<Playlist> = mutableListOf()
    private var activePriorityPlaylistId: UUID? = null
    private var currentPriorityTrackPlaylistItem: TrackPlaylistItem? = null

    private var activeRegularPlaylist: Playlist? = null
    private var activePriorityPlaylist: Playlist? = null

    init {
        logger.info { "Consumer components registered: $consumers" }
    }

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationConfigurationPersistEvent -> configuration.writeConfiguration()
            is SubPlaylistActivatePriorityPlaylistEvent -> activatePriorityPlaylist(event.playlistId)
            is SubPlaylistActivateRegularPlaylistEvent -> activateRegularPlaylist(event.playlistId)
            is SubPlaylistPlayNextEvent -> playNext()
        }
    }

    override fun boot() {
        // do nothing
    }

    override fun afterStartup() {
        configuration.readConfiguration()

        logger.debug { "Notify components about playlist manager readiness" }
        consumers.forEach { it.playlistManagerReady(this) }

        logger.debug { "Ask components to register their playlists" }
        consumers.forEach { it.registerPlaylist() }
    }

    override fun beforeShutdown() {
        configuration.writeConfiguration()
    }

    @Synchronized
    fun activateRegularPlaylist(playlistId: String?) {
        activeRegularPlaylist = regularPlaylists.firstOrNull { playlist ->
            playlist.id() == playlistId
        } ?: regularPlaylists.first() // TODO unsafe if previously there was activeRegularPlaylist
    }

    @Synchronized
    fun activatePriorityPlaylist(playlistId: String?) {
        activePriorityPlaylist = priorityPlaylists.firstOrNull { playlist ->
            playlist.id() == playlistId
        } ?: priorityPlaylists.firstOrNull() // TODO unsafe if previously there was priorityRegularPlaylist
    }

    @Synchronized
    fun playCurrent(): TrackPlaylistItem? {
        val trackPlaylistItem = activePriorityPlaylist?.moveToCurrentTrack() ?: activeRegularPlaylist?.moveToCurrentTrack()

        if (trackPlaylistItem != null) {
            publish(SubPlaybackStartEvent(trackPlaylistItem.track))
        }

        return trackPlaylistItem
    }

    @Synchronized
    fun playNext(): TrackPlaylistItem? {
        val trackPlaylistItem = activePriorityPlaylist?.moveToNextTrack() ?: activeRegularPlaylist?.moveToNextTrack()

        if (trackPlaylistItem != null) {
            publish(SubPlaybackStartEvent(trackPlaylistItem.track))
        }

        return trackPlaylistItem
    }

    @Synchronized
    fun getPlaylist(playlistId: String, holder: MutableList<Playlist>): Playlist? {
        return holder.firstOrNull { it.id() == playlistId }
    }

    @Synchronized
    fun getPlaylist(playlistId: String): Playlist? {
        return getPlaylist(playlistId, regularPlaylists) ?: getPlaylist(playlistId, priorityPlaylists)
    }

    @Synchronized
    fun addPlaylist(playlist: Playlist) {
        when (playlist) {
            is Queue -> addPlaylist(playlist, priorityPlaylists)
            else -> addPlaylist(playlist, regularPlaylists)
        }
    }

    @Synchronized
    private fun addPlaylist(playlist: Playlist, holder: MutableList<Playlist>) {
        if (getPlaylist(playlist.id(), holder) == null) {
            holder += playlist
        }
    }

    inner class Configuration {

        private val playlistDir: Path = configHomeDir.resolve("playlist")

        init {
            playlistDir.createDirectories()
        }

        fun readConfiguration() {
            val playlistConfigFiles = try {
                Files.list(playlistDir).toList()
            } catch (e: Exception) {
                listOf()
            }.also {
                logger.info { "Discovered playlist files: $it" }
            }

            playlistConfigFiles.map { playlistConfigFile ->
                logger.debug { "Read playlist from $playlistConfigFile" }

                try {
                    addPlaylist(Json.decodeFromString(Files.readString(playlistConfigFile))).also {
                        logger.trace { "Playlist loaded: $it" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to read playlist from $playlistConfigFile" }
                }
            }
        }

        fun writeConfiguration() {
            writePlaylists(regularPlaylists)
            writePlaylists(priorityPlaylists)
        }

        private fun writePlaylists(playlists: List<Playlist>) {
            playlists.forEach { playlist ->
                val playlistId = playlist.id()
                val playlistAsString = Json.encodeToString(playlist).also {
                    logger.trace { "Playlist ($playlistId) to be persisted: $it" }
                }
                val playlistFile = Path.of(playlistDir.toString(), playlistId).also {
                    logger.debug { "Write playlist to $it" }
                }
                try {
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

}