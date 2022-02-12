package org.gravidence.gravifon.configuration

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.gravidence.gravifon.util.serialization.gravifonSerializer

interface Configurable {

    var settings: Settings

}

inline fun <reified T> Configurable.writeConfig(config: T) {
    val configAsString = gravifonSerializer.encodeToString(config)

    settings.componentConfig(this.javaClass.name, configAsString)
}

inline fun <reified T> Configurable.readConfig(defaultConfig: () -> T): T {
    val configAsString = settings.componentConfig(this.javaClass.name)

    return if (configAsString == null) {
        defaultConfig()
    } else {
        gravifonSerializer.decodeFromString(configAsString)
    }
}