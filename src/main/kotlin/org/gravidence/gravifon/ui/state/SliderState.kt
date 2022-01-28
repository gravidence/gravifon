package org.gravidence.gravifon.ui.state

import androidx.compose.runtime.MutableState

class SliderState(val position: MutableState<Float>, val positionRange: MutableState<ClosedFloatingPointRange<Float>>) {
}