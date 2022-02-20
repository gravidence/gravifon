package org.gravidence.gravifon.orchestration.marker

import org.gravidence.gravifon.configuration.ConfigUtil
import org.gravidence.gravifon.configuration.FileStorage
import java.nio.file.Path

/**
 * Represents a component with state, e.g. playlist file(s).
 */
interface Stateful {

    val fileStorage: FileStorage

    val fileStorageHomeDir: Path
        get() = ConfigUtil.configHomeDir.resolve("plugin")

}