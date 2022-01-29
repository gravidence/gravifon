package org.gravidence.gravifon.event.playlist

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.PlaylistItem

class SubPlaylistPlayCurrentEvent(val playlist: Playlist, val playlistItem: PlaylistItem? = null) : Event {
}