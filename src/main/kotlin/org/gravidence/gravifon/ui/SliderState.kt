package org.gravidence.gravifon.ui

import androidx.compose.runtime.MutableState

class SliderState(val position: MutableState<Float>, val steps: MutableState<Int>, val positionRange: MutableState<ClosedFloatingPointRange<Float>>) {
}