package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ConfigUtil.settingsFile
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventHandler
import org.gravidence.gravifon.event.application.*
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

private val logger = KotlinLogging.logger {}

@Component
class Settings : EventHandler() {

    @Serializable
    data class GConfig(var placeholder: String = "")

    private var config: GConfig = GConfig()

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationStartupEvent -> read()
            is SubApplicationShutdownEvent -> write()
            is SubApplicationConfigurationUpdateEvent -> update(event)
            is SubApplicationConfigurationPersistEvent -> write()
        }
    }

    private fun read() {
        logger.info { "Read application configuration from $settingsFile" }

        try {
            config = Json.decodeFromString<GConfig>(Files.readString(settingsFile))
        } catch (e: Exception) {
            logger.error(e) { "Failed to read application configuration from $settingsFile" }
        }

        publish(PubApplicationConfigurationAnnounceEvent(config.copy()))
    }

    private fun write() {
        val configAsString = Json.encodeToString(config).also {
            logger.debug { "Application configuration to be persisted: $it" }
        }
        logger.info { "Write application configuration to $settingsFile" }
        try {
            Files.writeString(
                settingsFile,
                configAsString,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            )
        } catch (e: Exception) {
            logger.error(e) { "Failed to write application configuration to $settingsFile" }
        }
    }

    private fun update(event: SubApplicationConfigurationUpdateEvent) {
        TODO("Not yet implemented")
    }

}