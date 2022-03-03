package org.gravidence.gravifon.plugin

import org.gravidence.gravifon.orchestration.marker.Configurable

interface Plugin : Configurable {

    var pluginEnabled: Boolean

    val pluginDisplayName: String
    val pluginDescription: String

}