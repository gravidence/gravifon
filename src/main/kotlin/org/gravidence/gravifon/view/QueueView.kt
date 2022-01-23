package org.gravidence.gravifon.view

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PubApplicationReadyEvent
import org.gravidence.gravifon.event.component.PubPlaylistManagerReadyEvent
import org.gravidence.gravifon.event.component.PubSettingsReadyEvent
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.springframework.stereotype.Component

@Component
class QueueView(override var playlistId: String? = null) : View() {

    private lateinit var playlistManager: PlaylistManager

    override fun consume(event: Event) {
        when (event) {
            is PubSettingsReadyEvent -> playlistId = "b743b278-413d-4d47-b673-b2a26e4bbdc4"
            is PubPlaylistManagerReadyEvent -> playlistManager = event.playlistManager
            is PubApplicationReadyEvent -> {
                if (playlistId == null) {
                    playlistManager.addPlaylist(Queue())
                }
            }
        }
    }

}