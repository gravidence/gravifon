package org.gravidence.gravifon.configuration

import java.nio.file.Path
import kotlin.io.path.createDirectories

abstract class FileStorage(val storageDir: Path) {

    init {
        storageDir.createDirectories()
    }

    abstract fun read()

    abstract fun write()

}