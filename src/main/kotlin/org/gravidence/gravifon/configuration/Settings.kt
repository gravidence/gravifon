package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gravidence.gravifon.configuration.ConfigUtil.appConfigFile
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.EventConsumerIO
import org.gravidence.gravifon.event.application.ApplicationConfigurationAvailableEvent
import org.gravidence.gravifon.event.application.ApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.application.ApplicationShutdownEvent
import org.gravidence.gravifon.event.application.ApplicationStartupEvent
import org.gravidence.gravifon.library.Library
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

@Component
class Settings(val library: Library) : EventConsumerIO() {

    @Serializable
    data class AppConfig(var libraryRoots: List<String>)

    var appConfig: AppConfig? = null

    override fun consume(event: Event) {
        when (event) {
            is ApplicationStartupEvent -> read()
            is ApplicationShutdownEvent -> write()
            is ApplicationConfigurationPersistEvent -> write()
        }
    }

    private fun read() {
        val latestAppConfig = AppConfig(listOf("/home/m2/Library/"))

        appConfig = latestAppConfig

        EventBus.publish(ApplicationConfigurationAvailableEvent(latestAppConfig))
    }

    private fun write() {
        if (appConfig != null) {
            Files.writeString(
                appConfigFile,
                Json.encodeToString(appConfig),
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        }
    }

}