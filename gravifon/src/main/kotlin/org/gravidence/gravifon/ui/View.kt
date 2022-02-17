package org.gravidence.gravifon.ui

import androidx.compose.runtime.Composable
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext

private val logger = KotlinLogging.logger {}

/**
 * Marker for a component that it has own composable view to render.
 */
interface View {

    fun viewDisplayName(): String

    /**
     * Actions to be done upon view activation. DO NOT OVERRIDE, consider [activateExtra] instead.
     */
    fun activate() {
        GravifonContext.activeView.value = this.also {
            logger.debug { "${viewDisplayName()} view activated" }
        }

        activateExtra()
    }

    /**
     * View specific actions to be done upon its activation. Proper entry point to override since default implementation do nothing.
     */
    fun activateExtra() {

    }

    @Composable
    fun composeView()

}