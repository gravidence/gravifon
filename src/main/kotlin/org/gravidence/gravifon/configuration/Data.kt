package org.gravidence.gravifon.configuration

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.Gravifon.scopeIO
import org.gravidence.gravifon.domain.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventConsumerIO
import org.gravidence.gravifon.event.application.PubApplicationConfigurationAnnounceEvent
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.application.SubApplicationShutdownEvent
import org.gravidence.gravifon.library.Library
import org.gravidence.gravifon.library.Root
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

private val logger = KotlinLogging.logger {}

@Component
class Data(val playlistManager: PlaylistManager, private val library: Library) : EventConsumerIO() {

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationShutdownEvent -> scopeIO.launch { write() }
            is PubApplicationConfigurationAnnounceEvent -> scopeIO.launch { read(event) }
            is SubApplicationConfigurationPersistEvent -> scopeIO.launch { write() }
        }
    }

    private fun read(event: PubApplicationConfigurationAnnounceEvent) {
        logger.info { "Read application data: START" }

        logger.info { "Read library roots: ${event.config.library.roots}" }

        if (event.config.library.roots.isNotEmpty()) {
            val rootsFromConfigDir: List<Root> = event.config.library.roots
                .map {
                    val rootId = ConfigUtil.encode(it.rootDir)
                    val rootConfigFile = Path.of(ConfigUtil.libraryDir.toString(), rootId).also {
                        logger.debug { "Read library root configuration from $it" }
                    }

                    val rootTracksFromConfig: MutableList<VirtualTrack> = try {
                        Json.decodeFromString(Files.readString(rootConfigFile))
                    } catch (e: Exception) {
                        logger.error(e) { "Failed to read library root configuration from $rootConfigFile" }

                        mutableListOf()
                    }

                    Root(
                        rootDir = it.rootDir,
                        watchForChanges = it.watchForChanges,
                        scanOnInit = it.scanOnInit,
                        tracks = rootTracksFromConfig
                    ).also {
                        logger.trace { "Library root configuration loaded: ${it.tracks}" }
                    }
                }
            library.init(roots = rootsFromConfigDir.toMutableList())
        }

        logger.info { "Read application data: END" }
    }

    private fun write() {
        logger.info { "Write application data: START" }

        if (library.getRoots().isNotEmpty()) {
            library.getRoots().forEach {
                val rootId = ConfigUtil.encode(it.rootDir)
                val libraryRootConfigAsString = Json.encodeToString(it.tracks).also {
                    logger.trace { "Library root ($rootId) configuration to be persisted: $it" }
                }
                val rootConfigFile = Path.of(ConfigUtil.libraryDir.toString(), rootId).also {
                    logger.debug { "Write library root ($rootId) configuration to $it" }
                }
                try {
                    Files.writeString(
                        rootConfigFile,
                        libraryRootConfigAsString,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                }
                catch (e: Exception) {
                    logger.error(e) { "Failed to write library root ($rootId) configuration to $rootConfigFile" }
                }
            }
        }

        logger.info { "Write application data: END" }
    }

}