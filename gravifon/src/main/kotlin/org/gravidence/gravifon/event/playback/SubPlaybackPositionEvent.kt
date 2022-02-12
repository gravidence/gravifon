package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.event.Event
import kotlin.time.Duration

class SubPlaybackPositionEvent(val position: Duration) : Event {
}