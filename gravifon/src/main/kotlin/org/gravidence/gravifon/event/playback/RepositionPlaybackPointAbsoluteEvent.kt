package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.event.Event
import kotlin.time.Duration

/**
 * Move to specific playback point.
 */
class RepositionPlaybackPointAbsoluteEvent(val position: Duration) : Event