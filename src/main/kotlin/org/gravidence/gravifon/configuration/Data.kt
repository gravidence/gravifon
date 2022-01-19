package org.gravidence.gravifon.configuration

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
        if (event.config.library.roots.isNotEmpty()) {
            val rootsFromConfigDir: List<Root> = event.config.library.roots
                .map {
                    val rootId = ConfigUtil.encode(it.rootDir)
                    val rootConfigFile = Path.of(ConfigUtil.libraryDir.toString(), rootId)
                    val rootTracksFromConfig: MutableList<VirtualTrack> = try {
                        Json.decodeFromString(Files.readString(rootConfigFile))
                    } catch (e: Exception) {
                        // TODO log error
                        mutableListOf()
                    }

                    Root(
                        rootDir = it.rootDir,
                        watchForChanges = it.watchForChanges,
                        scanOnInit = it.scanOnInit,
                        tracks = rootTracksFromConfig
                    )
                }
            library.init(roots = rootsFromConfigDir.toMutableList())
        }
    }

    private fun write() {
        if (library.getRoots().isNotEmpty()) {
            library.getRoots().forEach {
                val rootId = ConfigUtil.encode(it.rootDir)
                val rootConfigFile = Path.of(ConfigUtil.libraryDir.toString(), rootId)
                Files.writeString(
                    rootConfigFile,
                    Json.encodeToString(it.tracks),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            }
        }
    }

}