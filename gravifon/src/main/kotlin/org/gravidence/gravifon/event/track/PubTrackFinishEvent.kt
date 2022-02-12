package org.gravidence.gravifon.event.track

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

class PubTrackFinishEvent(override val track: VirtualTrack, val duration: Duration, val timestamp: Instant = Clock.System.now()) : TrackEvent()