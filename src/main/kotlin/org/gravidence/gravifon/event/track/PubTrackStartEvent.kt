package org.gravidence.gravifon.event.track

import org.gravidence.gravifon.domain.track.VirtualTrack

class PubTrackStartEvent(override val track: VirtualTrack) : TrackEvent() {
}