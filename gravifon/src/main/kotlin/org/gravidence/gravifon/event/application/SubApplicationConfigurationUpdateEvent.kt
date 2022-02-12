package org.gravidence.gravifon.event.application

import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.event.Event

class SubApplicationConfigurationUpdateEvent(val config: Settings.GConfig): Event {
}