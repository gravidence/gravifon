package org.gravidence.gravifon.orchestration.marker

import androidx.compose.runtime.Composable
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext

private val logger = KotlinLogging.logger {}

/**
 * Represents a component which has a composable view to render.
 */
interface Viewable {

    var viewEnabled: Boolean
    val viewDisplayName: String

    /**
     * Actions to be done upon view activation. DO NOT OVERRIDE, consider [activateViewExtra] instead.
     */
    fun activateView() {
        GravifonContext.activeView.value = this.also {
            logger.debug { "View activated: $viewDisplayName" }
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