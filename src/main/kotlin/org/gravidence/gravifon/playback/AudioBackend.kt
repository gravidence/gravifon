package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

interface AudioBackend {

    fun play()
    fun pause()
    fun stop()

    /**
     * Returns stream length.
     */
    fun queryLength(): Duration
    /**
     * Returns current stream position.
     */
    fun queryPosition(): Duration
    /**
     * Adjusts stream current position.
     */
    fun adjustPosition(position: Duration)
    fun queryVolume(): Int
    fun adjustVolume(volume: Int)

    fun prepare(track: VirtualTrack)
    fun prepareNext(track: VirtualTrack)

}