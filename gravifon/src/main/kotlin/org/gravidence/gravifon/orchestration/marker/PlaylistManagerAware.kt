package org.gravidence.gravifon.orchestration.marker

import org.gravidence.gravifon.playlist.manage.PlaylistManager

interface PlaylistManagerAware {

    val playlistManager: PlaylistManager

}