package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.event.Event
import kotlin.time.Duration

/**
 * Shift playback point either forward or backward by some [positionDelta], supplying positive or negative value respectively.
 */
class RepositionPlaybackPointRelativeEvent(val positionDelta: Duration) : Event