package org.gravidence.gravifon.orchestration

import org.gravidence.gravifon.playlist.manage.PlaylistManager

interface PlaylistManagerConsumer {

    fun playlistManagerReady(playlistManager: PlaylistManager)
    fun registerPlaylist()

}