package org.gravidence.gravifon.event.track

import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event

abstract class TrackEvent() : Event {

    /**
     * The track to handle.
     */
    abstract val track: VirtualTrack

}