package org.gravidence.gravifon.plugin.library

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.FileStorage
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.orchestration.marker.Stateful
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

private val logger = KotlinLogging.logger {}

@Component
class LibraryStorage : Stateful {

    override val fileStorage: FileStorage = LibraryFileStorage()

    private val roots: MutableList<Root> = ArrayList()

    init {
        fileStorage.read()

        logger.info { "Initialize library (${roots.size} roots configured)" }

        GravifonContext.scopeIO.launch {
            this@LibraryStorage.roots
                .filter { it.scanOnInit }
                .forEach { it.scan() }
        }
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
    fun allTracks(): List<VirtualTrack> {
        return roots
            .flatMap { root -> root.tracks }
            .toList()
    }

    inner class LibraryFileStorage : FileStorage(storageDir = fileStorageHomeDir.resolve("library")) {

        override fun read() {
            val libraryRootConfigFiles = try {
                Files.list(storageDir).toList()
            } catch (e: Exception) {
                listOf()
            }.also {
                logger.info { "Discovered library root configuration files: $it" }
            }

            libraryRootConfigFiles.map { libraryRootConfigFile ->
                logger.debug { "Read library root configuration from $libraryRootConfigFile" }

                try {
                    addRoot(gravifonSerializer.decodeFromString(Files.readString(libraryRootConfigFile))).also {
                        logger.trace { "Library root configuration loaded: $it" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to read library root configuration from $libraryRootConfigFile" }
                }
            }
        }

        override fun write() {
            getRoots().forEach {
                val rootId = encode(it.rootDir)
                val libraryRootConfigAsString = gravifonSerializer.encodeToString(it).also {
                    logger.trace { "Library root ($rootId) configuration to be persisted: $it" }
                }
                val rootConfigFile = Path.of(storageDir.toString(), rootId).also {
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

        @OptIn(ExperimentalEncodingApi::class)
        private fun encode(originalPath: String): String {
            return Base64.UrlSafe.encode(originalPath.encodeToByteArray())
        }

    }

}