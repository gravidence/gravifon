package org.gravidence.gravifon.event.component

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.playlist.manage.PlaylistManager

class PubPlaylistManagerReadyEvent(val playlistManager: PlaylistManager): Event {
}