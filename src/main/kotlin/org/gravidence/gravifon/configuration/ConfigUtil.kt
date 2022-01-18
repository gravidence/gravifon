package org.gravidence.gravifon.configuration

import org.springframework.util.Base64Utils
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

object ConfigUtil {

    private const val configDirName = ".gravifon"
    private const val configHomeEnvVar = "GRAVIFON_CONFIG_HOME"

    val configHomeDir = resolveConfigHome()
    val appConfigFile = configHomeDir.resolve("config")
    val rootsConfigDir = configHomeDir.resolve("library")

    init {
        if (true) {
            configHomeDir.createDirectories()
            rootsConfigDir.createDirectories()
        }
    }

    private fun resolveConfigHome(): Path {
        val customDirEnv = System.getenv(configHomeEnvVar)
        if (customDirEnv != null) {
            val path = Path.of(customDirEnv)
            if (path.isDirectory()) {
                return path
            }
        }

        // https://docs.oracle.com/javase/6/docs/api/java/lang/System.html#getProperties()

        // TODO provide a way to force creating portable setup
        val currentDir = Path.of(System.getProperty("user.dir"), configDirName)
        if (currentDir.exists()) {
            return currentDir
        }

        val homeDir = Path.of(System.getProperty("user.home"), configDirName)

        return homeDir
    }

    fun pathToId(path: String): String {
        return Base64Utils.encodeToUrlSafeString(path.encodeToByteArray())
    }

    fun idToPath(id: String): String {
        return String(Base64Utils.decodeFromUrlSafeString(id))
    }

}