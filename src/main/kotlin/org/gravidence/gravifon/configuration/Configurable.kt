package org.gravidence.gravifon.configuration

interface Configurable {

    var settings: Settings

    fun readConfig(): String? {
        return settings.componentConfig(this.javaClass.name)
    }

    fun writeConfig(config: String) {
        settings.componentConfig(this.javaClass.name, config)
    }

}