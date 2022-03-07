package org.gravidence.gravifon.event.track

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

class TrackFinishedEvent(
    override val track: VirtualTrack,
    /**
     * The duration the track actually played (could less or more than track length).
     */
    val duration: Duration,
    /**
     * The time the track finished playing.
     */
    val timestamp: Instant = Clock.System.now()
) : TrackEvent()