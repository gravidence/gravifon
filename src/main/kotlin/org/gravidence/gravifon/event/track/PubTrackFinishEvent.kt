package org.gravidence.gravifon.event.track

import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

class PubTrackFinishEvent(track: VirtualTrack, duration: Duration) : TrackEvent(track) {
}