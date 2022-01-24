package org.gravidence.gravifon.library

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.Gravifon
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.component.PubLibraryReadyEvent
import org.gravidence.gravifon.orchestration.OrchestratorConsumer
import org.gravidence.gravifon.plugin.Plugin
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.streams.toList

private val logger = KotlinLogging.logger {}

@Component
class Library : Plugin(), OrchestratorConsumer {

    private val configuration = Configuration()
    private val roots: MutableList<Root> = ArrayList()

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationConfigurationPersistEvent -> configuration.writeConfiguration()
        }
    }

    override fun startup() {
        // do nothing
    }

    override fun afterStartup() {
        configuration.readConfiguration()
    }

    override fun beforeShutdown() {
        configuration.writeConfiguration()
    }

    @Synchronized
    private fun init() {
        logger.info { "Initialize library (${roots.size} roots configured)" }

        Gravifon.scopeIO.launch {
            this@Library.roots
                .filter { it.scanOnInit }
                .forEach { it.scan() }
        }

        publish(PubLibraryReadyEvent(this))
    }

    @Synchronized
    fun getRoots(): List<Root> {
        return roots.toList()
    }

    @Synchronized
    fun addRoot(root: Root) {
        if (roots.none { it.rootDir == root.rootDir }) {
            roots.add(root).also {
                logger.info { "Register library root: ${root.rootDir}" }
            }
        }
    }

    @Synchronized
    fun random(): VirtualTrack {
        return roots.first().tracks.random()
    }

    inner class Configuration {

        private val libraryDir: Path = configHomeDir.resolve("library")

        init {
            libraryDir.createDirectories()
        }

        fun readConfiguration() {
            val libraryRootConfigFiles = try {
                Files.list(libraryDir).toList()
            } catch (e: Exception) {
                listOf()
            }.also {
                logger.info { "Discovered library root configuration files: $it" }
            }

            libraryRootConfigFiles.map { libraryRootConfigFile ->
                logger.debug { "Read library root configuration from $libraryRootConfigFile" }

                try {
                    addRoot(Json.decodeFromString(Files.readString(libraryRootConfigFile))).also {
                        logger.trace { "Library root configuration loaded: $it" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to read library root configuration from $libraryRootConfigFile" }
                }
            }

            init()
        }

        fun writeConfiguration() {
            getRoots().forEach {
                val rootId = encode(it.rootDir)
                val libraryRootConfigAsString = Json.encodeToString(it).also {
                    logger.trace { "Library root ($rootId) configuration to be persisted: $it" }
                }
                val rootConfigFile = Path.of(libraryDir.toString(), rootId).also {
                    logger.debug { "Write library root configuration to $it" }
                }
                try {
                    Files.writeString(
                        rootConfigFile,
                        libraryRootConfigAsString,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                } catch (e: Exception) {
                    logger.error(e) { "Failed to write library root configuration to $rootConfigFile" }
                }
            }
        }


        fun encode(originalPath: String): String {
            return Base64Utils.encodeToUrlSafeString(originalPath.encodeToByteArray())
        }

        fun decode(encodedPath: String): String {
            return String(Base64Utils.decodeFromUrlSafeString(encodedPath))
        }

    }

}