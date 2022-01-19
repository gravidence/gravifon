package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.VirtualTrack

interface AudioBackend {

    fun play(track: VirtualTrack)
    fun pause()
    fun stop()

    fun prepareNext()

}