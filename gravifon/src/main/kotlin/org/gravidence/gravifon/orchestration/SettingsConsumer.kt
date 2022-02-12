package org.gravidence.gravifon.orchestration

import org.gravidence.gravifon.configuration.Configurable
import org.gravidence.gravifon.configuration.Settings

interface SettingsConsumer : Configurable {

    fun settingsReady(settings: Settings)
    fun persistConfig()

}