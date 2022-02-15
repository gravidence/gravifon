package org.gravidence.gravifon.plugin

import androidx.compose.runtime.Composable
import org.gravidence.gravifon.configuration.ConfigUtil
import org.gravidence.gravifon.event.EventHandler
import java.nio.file.Path

abstract class Plugin(
    val pluginConfigHomeDir: Path = ConfigUtil.configHomeDir.resolve("plugin"),

    val title: String,
    val description: String,
) : EventHandler() {

    @Composable
    abstract fun composeSettings()

}