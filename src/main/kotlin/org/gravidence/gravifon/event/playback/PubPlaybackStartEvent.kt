package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event

class PubPlaybackStartEvent(val track: VirtualTrack, val length: Long) : Event {
}