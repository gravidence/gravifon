package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ConfigUtil.settingsFile
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventHandler
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.application.SubApplicationConfigurationUpdateEvent
import org.gravidence.gravifon.orchestration.OrchestratorConsumer
import org.gravidence.gravifon.orchestration.SettingsConsumer
import org.gravidence.gravifon.view.LibraryView
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.StandardOpenOption

private val logger = KotlinLogging.logger {}

@Component
class Settings(private val consumers: List<SettingsConsumer>) : EventHandler(), OrchestratorConsumer {

    @Serializable
    data class GConfig(val application: GApplication = GApplication(), val component: MutableMap<String, String> = mutableMapOf())

    @Serializable
    data class GApplication(var activeView: String = LibraryView::class.qualifiedName!!)

    private var config: GConfig = GConfig()

    init {
        logger.info { "Consumer components registered: $consumers" }
    }

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationConfigurationUpdateEvent -> update(event)
            is SubApplicationConfigurationPersistEvent -> write()
        }
    }

    override fun startup() {
        read()

        logger.debug { "Notify components about application configuration readiness" }
        consumers.forEach { it.settingsReady(this) }
    }

    override fun afterStartup() {
        // do nothing
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
            config = Json.decodeFromString(Files.readString(settingsFile))
        } catch (e: Exception) {
            logger.error(e) { "Failed to read application configuration from $settingsFile" }
        }
    }

    private fun write() {
        logger.debug { "Collect config updates from components" }
        consumers.forEach { it.persistConfig() }

        logger.debug { "Collect config updates from application itself" }
        GravifonContext.activeView.value?.let { config.application.activeView = it.javaClass.name }

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