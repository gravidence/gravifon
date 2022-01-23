package org.gravidence.gravifon.view

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PubApplicationReadyEvent
import org.gravidence.gravifon.event.component.PubLibraryReadyEvent
import org.gravidence.gravifon.event.component.PubPlaylistManagerReadyEvent
import org.gravidence.gravifon.event.component.PubSettingsReadyEvent
import org.gravidence.gravifon.library.Library
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.springframework.stereotype.Component

@Component
class LibraryView(override var playlistId: String? = null) : View() {

    private lateinit var playlistManager: PlaylistManager
    private lateinit var library: Library

    override fun consume(event: Event) {
        when (event) {
            is PubSettingsReadyEvent -> playlistId = "64ce227a-261e-4d52-ac28-83de7a08b6d8"
            is PubPlaylistManagerReadyEvent -> playlistManager = event.playlistManager
            is PubLibraryReadyEvent -> library = event.library
            is PubApplicationReadyEvent -> {
                if (playlistId == null) {
                    playlistManager.addPlaylist(DynamicPlaylist())
                }
            }
        }
    }

}