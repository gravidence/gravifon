package org.gravidence.gravifon.orchestration.marker

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.util.serialization.gravifonSerializer

interface Configurable {

    val settings: Settings

    /**
     * Entry point to persist component configuration.
     */
    fun writeConfig()

}

/**
 * Helper method to persist component configuration (a bean) to application settings.
 */
inline fun <reified T> Configurable.writeConfig(config: T) {
    val configAsString = gravifonSerializer.encodeToString(config)

    settings.componentConfig(this.javaClass.name, configAsString)
}

/**
 * Helper method to read component configuration (a bean) from application settings.
 * [defaultConfig] producer is used if no component configuration found.
 */
inline fun <reified T> Configurable.readConfig(defaultConfig: () -> T): T {
    val configAsString = settings.componentConfig(this.javaClass.name)

    return if (configAsString == null) {
        defaultConfig()
    } else {
        gravifonSerializer.decodeFromString(configAsString)
    }
}