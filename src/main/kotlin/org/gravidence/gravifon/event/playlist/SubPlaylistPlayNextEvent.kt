package org.gravidence.gravifon.event.playlist

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.playlist.Playlist

class SubPlaylistPlayNextEvent(val playlist: Playlist) : Event {
}