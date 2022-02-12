package org.gravidence.gravifon.playback

import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.springframework.stereotype.Component

@Component
class AudioFlow(private val playlistManager: PlaylistManager) {

    fun next(): VirtualTrack? {
        return GravifonContext.activePlaylist.value?.let { playlistManager.resolveNext(it)?.track }
    }

}