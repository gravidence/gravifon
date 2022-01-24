package org.gravidence.gravifon.view

import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.event.EventHandler

abstract class View : EventHandler() {

    protected abstract var settings: Settings

    fun readViewConfig(): String? {
        return settings.componentConfig(this.javaClass.name)
    }

    fun writeViewConfig(viewConfig: String) {
        settings.componentConfig(this.javaClass.name, viewConfig)
    }

//    @Composable
//    abstract fun compose()

}