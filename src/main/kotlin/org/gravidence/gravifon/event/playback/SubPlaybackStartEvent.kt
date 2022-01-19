package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.domain.VirtualTrack
import org.gravidence.gravifon.event.Event

class SubPlaybackStartEvent(val track: VirtualTrack) : Event {
}