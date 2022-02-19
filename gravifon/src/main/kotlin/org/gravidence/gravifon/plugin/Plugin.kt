package org.gravidence.gravifon.plugin

import androidx.compose.runtime.Composable
import org.gravidence.gravifon.configuration.ConfigUtil
import org.gravidence.gravifon.orchestration.marker.EventAware
import java.nio.file.Path

abstract class Plugin(
    val pluginDisplayName: String,
    val pluginDescription: String,
) : EventAware {

    val pluginConfigHomeDir: Path = ConfigUtil.configHomeDir.resolve("plugin")

    @Composable
    abstract fun composeSettings()

}