package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ConfigUtil.settingsFile
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventHandler
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.application.SubApplicationConfigurationUpdateEvent
import org.gravidence.gravifon.orchestration.marker.ShutdownAware
import org.gravidence.gravifon.orchestration.marker.Configurable
import org.gravidence.gravifon.plugin.library.Library
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

private val logger = KotlinLogging.logger {}

@Component
class Settings(@Lazy private val configurables: List<Configurable>) : EventHandler(), ShutdownAware {

    @Serializable
    data class GConfig(val application: GApplication = GApplication(), val component: MutableMap<String, String> = mutableMapOf())

    @Serializable
    data class GApplication(var activeViewId: String = Library::class.qualifiedName!!, var activePlaylistId: String? = null)

    private var config: GConfig = GConfig()

    init {
        read()
    }

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationConfigurationUpdateEvent -> update(event)
            is SubApplicationConfigurationPersistEvent -> write()
        }
    }

    override fun beforeShutdown() {
        write()
    }

    @Synchronized
    fun componentConfig(componentId: String): String? {
        return config.component[componentId]
    }

    @Synchronized
    fun componentConfig(componentId: String, componentConfig: String) {
        config.component[componentId] = componentConfig
    }

    fun applicationConfig(): GApplication {
        return config.application.copy()
    }

    private fun read() {
        logger.info { "Read application configuration from $settingsFile" }

        try {
            config = gravifonSerializer.decodeFromString(Files.readString(settingsFile))
        } catch (e: Exception) {
            logger.error(e) { "Failed to read application configuration from $settingsFile" }
        }
    }

    private fun write() {
        logger.debug { "Collect config updates from components" }
        configurables.forEach { it.writeConfig() }

        logger.debug { "Collect config updates from application itself" }
        GravifonContext.activeView.value?.let { config.application.activeViewId = it.javaClass.name }
        GravifonContext.activePlaylist.value?.let { config.application.activePlaylistId = it.id() }

        val configAsString = gravifonSerializer.encodeToString(config).also {
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