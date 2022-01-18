package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.domain.VirtualTrack

class PlaybackPauseEvent(track: VirtualTrack) : PlaybackEvent(track) {
}