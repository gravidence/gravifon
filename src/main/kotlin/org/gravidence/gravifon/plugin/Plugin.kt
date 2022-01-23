package org.gravidence.gravifon.plugin

import org.gravidence.gravifon.configuration.ConfigUtil
import org.gravidence.gravifon.event.EventHandler
import java.nio.file.Path

abstract class Plugin(var configHomeDir: Path = ConfigUtil.configHomeDir) : EventHandler() {
}