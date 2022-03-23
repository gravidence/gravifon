package org.gravidence.gravifon.playback.backend

import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.orchestration.marker.Configurable
import org.gravidence.gravifon.playback.PlaybackStatus
import kotlin.time.Duration

interface AudioBackend : Configurable {

    fun registerCallback(
        aboutToFinishCallback: () -> Unit,
        audioStreamChangedCallback: (played: VirtualTrack?, next: VirtualTrack?, playtime: Duration) -> Unit,
        audioStreamBufferingCallback: (percent: Int) -> Unit,
        endOfStreamCallback: () -> Unit,
        playbackFailureCallback: (played: VirtualTrack?, next: VirtualTrack?, playtime: Duration) -> Unit,
    )

    fun play(): PlaybackStatus
    fun pause(): PlaybackStatus
    fun stop(): PlaybackStatus

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