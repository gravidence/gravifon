package org.gravidence.gravifon.orchestration.marker

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.util.serialization.gravifonSerializer

private val logger = KotlinLogging.logger {}

/**
 * Represents a component which has configuration.
 * The configuration is stored in global application configuration.
 */
interface Configurable {

    val settings: Settings

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

        val configAsString = gravifonSerializer.encodeToString(componentConfiguration)

        settings.componentConfig(this.javaClass.name, configAsString)
    }

    /**
     * Reads component configuration (a bean) from application settings.
     */
    fun <T : ComponentConfiguration> readComponentConfiguration(defaultConfig: () -> T): T {
        val configAsString = settings.componentConfig(this.javaClass.name)

        return if (configAsString == null) {
            defaultConfig()
        } else {
            gravifonSerializer.decodeFromString<ComponentConfiguration>(configAsString) as T
        }.also {
            logger.debug { "Use component configuration: $it" }
        }
    }

}