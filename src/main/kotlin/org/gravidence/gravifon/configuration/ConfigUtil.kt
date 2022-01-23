package org.gravidence.gravifon.configuration

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.notExists

object ConfigUtil {

    private const val configHomeDirName = ".gravifon"
    private const val configHomeEnvVar = "GRAVIFON_CONFIG_HOME"

    val configHomeDir: Path = resolveConfigHomeDir()
    val settingsFile: Path = configHomeDir.resolve("config")

    init {
        configHomeDir.createDirectories()
    }

    private fun resolveConfigHomeDir(): Path {
        val customDirEnv = System.getenv(configHomeEnvVar)
        if (customDirEnv != null) {
            val path = Path.of(customDirEnv)
            if (path.notExists() || path.isDirectory()) {
                return path
            }
        }

        // https://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()

        // TODO provide a way to force creating portable setup
        val currentDir = Path.of(System.getProperty("user.dir"), configHomeDirName)
        if (currentDir.exists()) {
            return currentDir
        }

        val homeDir = Path.of(System.getProperty("user.home"), configHomeDirName)

        return homeDir
    }

}