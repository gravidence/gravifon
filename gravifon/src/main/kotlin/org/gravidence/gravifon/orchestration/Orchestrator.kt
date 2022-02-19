package org.gravidence.gravifon.orchestration

import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.Playable
import org.gravidence.gravifon.orchestration.marker.ShutdownAware
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Orchestrator(
    private val eventAwares: Collection<EventAware>,
    private val shutdownAwares: Collection<ShutdownAware>,
    private val viewables: Collection<Viewable>,
    private val playables: Collection<Playable>,
    private val configurationManager: ConfigurationManager
) {

    init {
        logger.debug { "Event aware components registered: $eventAwares" }
        logger.debug { "Shutdown aware components registered: $shutdownAwares" }
        logger.debug { "Viewable components registered: $viewables" }
        logger.debug { "Playable components registered: $playables" }
    }

    fun startup() {
        eventAwares.forEach {
            logger.info { "Subscribe ${it::class.simpleName} to event flow" }
            EventBus.subscribe(it::receive)
        }

        val activeViewId = configurationManager.applicationConfig().activeViewId
        // TODO fallback to default view if not configured
        viewables.find { it.javaClass.name == activeViewId }?.activateView()

        val activePlaylistId = configurationManager.applicationConfig().activePlaylistId
        // TODO fallback to default playlist if not configured
        playables.find { it.playlist.id() == activePlaylistId }?.activatePlaylist()
    }

    fun shutdown() {
        logger.info { "Shutdown routine, started" }
        shutdownAwares.forEach { it.beforeShutdown() }
        logger.info { "Shutdown routine, completed" }
    }

}