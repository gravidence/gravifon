package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.domain.VirtualTrack

class PlaybackStartEvent(track: VirtualTrack) : PlaybackEvent(track) {
}