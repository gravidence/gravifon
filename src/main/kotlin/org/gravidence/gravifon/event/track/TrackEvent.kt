package org.gravidence.gravifon.event.track

import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event

abstract class TrackEvent(val track: VirtualTrack) : Event {
}