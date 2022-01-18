package org.gravidence.gravifon.playlist.manage

import org.gravidence.gravifon.Initializable
import org.gravidence.gravifon.configuration.Data
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventConsumer
import org.gravidence.gravifon.event.playback.PlaybackStartEvent
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import java.util.*

@Component
class PlaylistManager: Initializable, EventConsumer() {
/*
    private val regularPlaylists: List<Playlist>
    activeRegularPlaylistId: UUID? = null
    private var currentRegularTrackPlaylistItem: TrackPlaylistItem? = null
    private val priorityPlaylists: List<Playlist>
    activePriorityPlaylistId: UUID? = null
    private var currentPriorityTrackPlaylistItem: TrackPlaylistItem? = null
    private val ctx: ApplicationContext

    private lateinit var activeRegularPlaylist: Playlist
    private lateinit var activePriorityPlaylist: Playlist

    init {
        activateRegularPlaylist(activeRegularPlaylistId)
        activatePriorityPlaylist(activePriorityPlaylistId)
    }

    fun activateRegularPlaylist(playlistId: UUID?) {
        activeRegularPlaylist = regularPlaylists.firstOrNull { playlist ->
            playlist.id() == playlistId
        } ?: regularPlaylists.first() // TODO unsafe if previously there was activeRegularPlaylist
    }

    fun activatePriorityPlaylist(playlistId: UUID?) {
        activePriorityPlaylist = priorityPlaylists.firstOrNull { playlist ->
            playlist.id() == playlistId
        } ?: priorityPlaylists.first() // TODO unsafe if previously there was priorityRegularPlaylist
    }

    fun playCurrent(): TrackPlaylistItem? {
        val trackPlaylistItem =
            activePriorityPlaylist.moveToCurrentTrack() ?: activeRegularPlaylist.moveToCurrentTrack()

        if (trackPlaylistItem != null) {
            ctx.publishEvent(PlaybackStartEvent(trackPlaylistItem.track))
        }

        return trackPlaylistItem
    }

    fun playNext(): TrackPlaylistItem? {
        val trackPlaylistItem = activePriorityPlaylist.moveToNextTrack() ?: activeRegularPlaylist.moveToNextTrack()

        if (trackPlaylistItem != null) {
            ctx.publishEvent(PlaybackStartEvent(trackPlaylistItem.track))
        }

        return trackPlaylistItem
    }
*/

    override fun isInitialized(): Boolean {
        return false
    }

    override fun consume(event: Event) {
//        TODO("Not yet implemented")
    }

}