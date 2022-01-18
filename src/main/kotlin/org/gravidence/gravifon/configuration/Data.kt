package org.gravidence.gravifon.configuration

import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gravidence.gravifon.Gravifon.scopeIO
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventConsumerIO
import org.gravidence.gravifon.event.application.ApplicationConfigurationAvailableEvent
import org.gravidence.gravifon.event.application.ApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.application.ApplicationShutdownEvent
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
//            is ApplicationStartupEvent -> read()
            is ApplicationShutdownEvent -> scopeIO.launch { write() }
            is ApplicationConfigurationAvailableEvent -> scopeIO.launch { read(event) }
            is ApplicationConfigurationPersistEvent -> scopeIO.launch { write() }
        }
    }

    private suspend fun read(event: ApplicationConfigurationAvailableEvent) {
        if (event.appConfig.libraryRoots.isNotEmpty()) {
            val rootsFromConfigDir: List<Root> = event.appConfig.libraryRoots
                .map {
                    val rootId = ConfigUtil.pathToId(it)
                    val rootConfigFile = Path.of(ConfigUtil.rootsConfigDir.toString(), rootId)
                    Json.decodeFromString(Files.readString(rootConfigFile))
                }
            library.init(roots = rootsFromConfigDir.toMutableList())
        }
    }

    private suspend fun write() {
        if (library.getRoots().isNotEmpty()) {
            library.getRoots().forEach {
                val rootId = ConfigUtil.pathToId(it.rootDir)
                val rootConfigFile = Path.of(ConfigUtil.rootsConfigDir.toString(), rootId)
                Files.writeString(
                    rootConfigFile,
                    Json.encodeToString(it),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            }
        }
    }

}