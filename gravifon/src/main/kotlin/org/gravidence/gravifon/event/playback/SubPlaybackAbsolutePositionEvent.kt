package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.event.Event
import kotlin.time.Duration

class SubPlaybackAbsolutePositionEvent(val position: Duration) : Event