package org.gravidence.gravifon.event.track

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.gravidence.gravifon.domain.track.VirtualTrack

class PubTrackStartEvent(
    override val track: VirtualTrack,
    /**
     * The time the track started playing.
     */
    val timestamp: Instant = Clock.System.now()
) : TrackEvent()