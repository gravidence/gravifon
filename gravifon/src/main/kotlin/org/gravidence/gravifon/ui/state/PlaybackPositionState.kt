package org.gravidence.gravifon.ui.state

import kotlin.time.Duration

data class PlaybackPositionState(
    val runningPosition: Duration = Duration.ZERO,
    val endingPosition: Duration = Duration.ZERO
)