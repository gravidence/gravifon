package org.gravidence.gravifon.plugin

import androidx.compose.runtime.Composable

/**
 * Marker for a component that it has own composable view to render.
 */
interface View {

    @Composable
    fun composeView()

}