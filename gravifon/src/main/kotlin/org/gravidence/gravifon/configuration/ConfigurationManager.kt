package org.gravidence.gravifon.configuration

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ConfigUtil.settingsFile
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PersistConfigurationEvent
import org.gravidence.gravifon.orchestration.marker.Configurable
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.ShutdownAware
import org.gravidence.gravifon.orchestration.marker.Stateful
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

private val logger = KotlinLogging.logger {}

@Component
class ConfigurationManager(@Lazy private val configurables: List<Configurable>, @Lazy private val statefuls: List<Stateful>) : EventAware, ShutdownAware {

    private val applicationConfiguration: GConfig

    init {
        applicationConfiguration = readApplicationConfiguration()
    }

    override fun consume(event: Event) {
        when (event) {
            is PersistConfigurationEvent -> persistEverything()
        }
    }

    override fun beforeShutdown() {
        persistEverything()
    }

    @Synchronized
    fun componentConfig(componentId: String): ComponentConfiguration? {
        return applicationConfiguration.component[componentId]
    }

    @Synchronized
    fun componentConfig(componentId: String, componentConfig: ComponentConfiguration) {
        applicationConfiguration.component[componentId] = componentConfig
    }

    fun applicationConfig(): GApplication {
        return applicationConfiguration.application.copy()
    }

    private fun readApplicationConfiguration(): GConfig {
        logger.info { "Read application configuration from $settingsFile" }

        return try {
            gravifonSerializer.decodeFromString(Files.readString(settingsFile))
        } catch (e: Exception) {
            logger.error(e) { "Failed to read application configuration from $settingsFile" }
            GConfig()
        }
    }

    private fun persistApplicationConfiguration() {
        logger.debug { "Collect component configuration updates: START" }
        configurables.forEach { it.writeComponentConfiguration() }
        logger.debug { "Collect component configuration updates: FINISH" }

        logger.debug { "Collect application configuration updates: START" }
        GravifonContext.activeView.value?.let { applicationConfiguration.application.activeViewId = it.viewDisplayName }
        GravifonContext.activePlaylist.value?.let { applicationConfiguration.application.activePlaylistId = it.id() }
        logger.debug { "Collect application configuration updates: END" }

        logger.debug { "Persist application configuration: START" }
        val configAsString = gravifonSerializer.encodeToString(applicationConfiguration).also {
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
        logger.debug { "Persist application configuration: END" }
    }

    private fun persistComponentStates() {
        logger.debug { "Persist component file storage: START" }
        statefuls.forEach { it.fileStorage.write() }
        logger.debug { "Persist component file storage: FINISH" }
    }

    private fun persistEverything() {
        persistComponentStates()
        persistApplicationConfiguration()
    }

}