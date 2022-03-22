package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.orchestration.marker.Configurable
import kotlin.time.Duration

interface AudioBackend : Configurable {

    fun registerCallback(
        aboutToFinishCallback: () -> Unit,
        audioStreamChangedCallback: (played: VirtualTrack?, next: VirtualTrack?, playtime: Duration) -> Unit,
        audioStreamBufferingCallback: (percent: Int) -> Unit,
        endOfStreamCallback: () -> Unit,
        playbackFailureCallback: (played: VirtualTrack?, next: VirtualTrack?, playtime: Duration) -> Unit,
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