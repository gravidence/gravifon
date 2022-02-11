package org.gravidence.gravifon.event.track

import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

class PubTrackFinishEvent(override val track: VirtualTrack, val duration: Duration) : TrackEvent() {
}