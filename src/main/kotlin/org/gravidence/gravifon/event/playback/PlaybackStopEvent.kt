package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.domain.VirtualTrack

class PlaybackStopEvent(track: VirtualTrack) : PlaybackEvent(track) {
}