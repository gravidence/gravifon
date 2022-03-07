package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event

/**
 * Low-level event to start playing specific [track].
 */
class StartPlaybackEvent(val track: VirtualTrack) : Event