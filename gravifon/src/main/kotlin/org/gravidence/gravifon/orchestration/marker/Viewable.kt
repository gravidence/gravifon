package org.gravidence.gravifon.orchestration.marker

import androidx.compose.runtime.Composable
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext

private val logger = KotlinLogging.logger {}

/**
 * Marker for a component that it has own composable view to render.
 */
interface Viewable {

    fun viewDisplayName(): String

    /**
     * Actions to be done upon view activation. DO NOT OVERRIDE, consider [activateViewExtra] instead.
     */
    fun activateView() {
        GravifonContext.activeView.value = this.also {
            logger.debug { "View activated: ${viewDisplayName()}" }
        }

        activateViewExtra()
    }

    /**
     * View specific actions to be done upon its activation. Proper entry point to override since default implementation do nothing.
     */
    fun activateViewExtra() {

    }

    @Composable
    fun composeView()

}