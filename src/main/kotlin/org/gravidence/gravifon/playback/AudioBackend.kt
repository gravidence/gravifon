package org.gravidence.gravifon.playback

import org.gravidence.gravifon.Initializable
import org.gravidence.gravifon.domain.track.VirtualTrack

interface AudioBackend : Initializable {

    fun play()
    fun pause()
    fun stop()

    fun queryLength(): Long
    fun queryPosition(): Long
    fun adjustPosition(position: Long)
    fun queryVolume(): Int
    fun adjustVolume(volume: Int)

    fun prepare(track: VirtualTrack)
    fun prepareNext(track: VirtualTrack)

}