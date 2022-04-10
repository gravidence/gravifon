package org.gravidence.gravifon.event.playback

import org.gravidence.gravifon.event.Event

/**
 * High-level event to stop playback after n-th track.
 */
class StopPlaybackAfterEvent(val n: Int = 0) : Event