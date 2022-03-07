package org.gravidence.gravifon.event.playlist

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.PlaylistItem

class PlayCurrentFromPlaylistEvent(
    val playlist: Playlist,
    /**
     * Specific playlist item to play, otherwise playlist manager will pick up the current.
     */
    val playlistItem: PlaylistItem? = null
) : Event