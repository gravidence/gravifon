package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

interface AudioBackend {

    fun registerCallback(
        aboutToFinishCallback: () -> Unit,
        audioStreamChangedCallback: (VirtualTrack?, Duration) -> Unit,
        endOfStreamCallback: () -> Unit
    )

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

    fun prepareNext(track: VirtualTrack)

}