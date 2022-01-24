package org.gravidence.gravifon.orchestration

import org.gravidence.gravifon.configuration.Settings

interface SettingsConsumer {

    fun settingsReady(settings: Settings)
    fun persistConfig()

}