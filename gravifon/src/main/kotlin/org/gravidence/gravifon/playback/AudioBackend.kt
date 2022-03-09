package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

interface AudioBackend {

    fun registerCallback(
        aboutToFinishCallback: () -> Unit,
        audioStreamChangedCallback: (VirtualTrack?, Duration) -> Unit,
        audioStreamBufferingCallback: (Int) -> Unit,
        endOfStreamCallback: () -> Unit,
        playbackFailureCallback: (VirtualTrack?, Duration) -> Unit,
    )

    fun play(): PlaybackState
    fun pause(): PlaybackState
    fun stop(): PlaybackState

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