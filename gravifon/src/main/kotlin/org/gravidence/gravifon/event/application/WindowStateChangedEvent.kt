package org.gravidence.gravifon.event.application

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import org.gravidence.gravifon.event.Event

/**
 * Emitted when application window state has been changed (e.g. size, position).
 */
class WindowStateChangedEvent(
    val size: DpSize? = null,
    val position: WindowPosition? = null,
    val placement: WindowPlacement? = null,
) : Event