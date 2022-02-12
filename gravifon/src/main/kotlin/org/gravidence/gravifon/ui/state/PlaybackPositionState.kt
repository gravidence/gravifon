package org.gravidence.gravifon.ui.state

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlin.time.Duration

class PlaybackPositionState(
    val runningPosition: MutableState<Duration> = mutableStateOf(Duration.ZERO),
    val endingPosition: MutableState<Duration> = mutableStateOf(Duration.ZERO)
) {
}