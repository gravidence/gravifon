package org.gravidence.gravifon.orchestration.marker

import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration

private val logger = KotlinLogging.logger {}

/**
 * Represents a component which has configuration.
 * The configuration is stored in global application configuration.
 */
interface Configurable : ConfigurationManagerAware {

    /**
     * Interface implementations must be registered in Gravifon serializers module.
     * See [documentation](https://github.com/Kotlin/kotlinx.serialization/blob/master/docs/polymorphism.md#serializing-interfaces).
     */
    val componentConfiguration: ComponentConfiguration

    /**
     * Writes component configuration (a bean) to application settings.
     */
    fun writeComponentConfiguration() {
        logger.debug { "Store component configuration: $componentConfiguration" }

        configurationManager.componentConfig(this.javaClass.name, componentConfiguration)
    }

    /**
     * Reads component configuration (a bean) from application settings.
     */
    fun <T : ComponentConfiguration> readComponentConfiguration(defaultConfig: () -> T): T {
        return ((configurationManager.componentConfig(this.javaClass.name) as? T) ?: defaultConfig()).also {
            logger.debug { "Use component configuration: $it" }
        }
    }

}