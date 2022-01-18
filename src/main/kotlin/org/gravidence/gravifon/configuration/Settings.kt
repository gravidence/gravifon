package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gravidence.gravifon.configuration.ConfigUtil.settingsFile
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.EventConsumerIO
import org.gravidence.gravifon.event.application.*
import org.gravidence.gravifon.library.Library
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class Settings(val library: Library) : EventConsumerIO() {

    @Serializable
    data class GConfig(val library: GLibrary = GLibrary())

    @Serializable
    data class GLibrary(val roots: MutableList<GRoot> = mutableListOf())

    @Serializable
    data class GRoot(
        var rootDir: String,
        var watchForChanges: Boolean = false,
        var scanOnInit: Boolean = false,
    )

    private var config: GConfig = GConfig()

    override fun consume(event: Event) {
        when (event) {
            is ApplicationStartupEvent -> read()
            is ApplicationShutdownEvent -> write()
            is ApplicationConfigurationUpdateEvent -> update(event)
            is ApplicationConfigurationPersistEvent -> write()
        }
    }

    private fun read() {
        try {
            config = Json.decodeFromString(Files.readString(settingsFile))
        } catch (e: Exception) {
            // TODO log an error then either keep previous config instance or create a default one
        }

        EventBus.publish(ApplicationConfigurationAnnounceEvent(config.copy()))
    }

    private fun write() {
        Files.writeString(
            settingsFile,
            Json.encodeToString(config),
            StandardOpenOption.CREATE,
            StandardOpenOption.WRITE,
            StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    private fun update(event: ApplicationConfigurationUpdateEvent) {
        TODO("Not yet implemented")
    }

}