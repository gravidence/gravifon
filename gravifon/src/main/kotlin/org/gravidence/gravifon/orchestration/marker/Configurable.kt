package org.gravidence.gravifon.orchestration.marker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration

private val logger = KotlinLogging.logger {}

/**
 * Represents a component which has configuration.
 * The configuration is stored in global application configuration.
 */
interface Configurable : ConfigurationManagerAware {

    val componentConfiguration: MutableState<out ComponentConfiguration>

    /**
     * Writes component configuration (a bean) to application settings.
     */
    fun writeComponentConfiguration() {
        logger.debug { "Store component configuration: ${componentConfiguration.value}" }

        configurationManager.componentConfig(this.javaClass.name, componentConfiguration.value)
    }

    /**
     * Reads component configuration (a bean) from application settings.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : ComponentConfiguration> readComponentConfiguration(defaultConfig: () -> T): T {
        return ((configurationManager.componentConfig(this.javaClass.name) as? T) ?: defaultConfig()).also {
            logger.debug { "Use component configuration: $it" }
        }
    }

    @Composable
    fun composeSettings()

}