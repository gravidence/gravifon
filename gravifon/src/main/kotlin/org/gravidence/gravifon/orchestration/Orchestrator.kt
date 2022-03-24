package org.gravidence.gravifon.orchestration

import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.ApplicationStartedEvent
import org.gravidence.gravifon.event.playback.SelectAudioBackendEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.Playable
import org.gravidence.gravifon.orchestration.marker.ShutdownAware
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playback.backend.AudioBackend
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Orchestrator(
    private val eventAwares: Collection<EventAware>,
    private val shutdownAwares: Collection<ShutdownAware>,
    private val viewables: Collection<Viewable>,
    private val playables: Collection<Playable>,
    private val audioBackends: Collection<AudioBackend>,
    private val configurationManager: ConfigurationManager,
) {

    init {
        logger.debug { "Event aware components registered: $eventAwares" }
        logger.debug { "Shutdown aware components registered: $shutdownAwares" }
        logger.debug { "Viewable components registered: $viewables" }
        logger.debug { "Playable components registered: $playables" }
        logger.debug { "Audio backend components registered: $audioBackends" }
    }

    fun startup() {
        logger.info { "Startup routine, started" }

        eventAwares.forEach {
            logger.info { "Subscribe ${it::class.simpleName} to event flow" }
            EventBus.subscribe(it::receive)
        }

        val applicationConfig = configurationManager.applicationConfig()

        val activeViewId = applicationConfig.activeViewId
        val activeView = viewables.find { it.viewDisplayName == activeViewId } ?: defaultView()
        activeView.activateView()

        val activePlaylistId = applicationConfig.activePlaylistId
        val activePlaylist = playables.find { it.playlist.id() == activePlaylistId } ?: defaultPlaylist(activeView)
        activePlaylist?.activatePlaylist()

        val audioBackend = audioBackends.find { it.id == applicationConfig.activeAudioBackendId } ?: defaultAudioBackend()
        EventBus.publish(SelectAudioBackendEvent(audioBackend))

        EventBus.publish(ApplicationStartedEvent())

        logger.info { "Startup routine, completed" }
    }

    fun shutdown() {
        logger.info { "Shutdown routine, started" }

        shutdownAwares.forEach { it.beforeShutdown() }

        logger.info { "Shutdown routine, completed" }
    }

    private fun defaultView(): Viewable {
        return viewables.find { it is Playable } ?: viewables.first()
    }

    private fun defaultPlaylist(activeView: Viewable): Playable? {
        return activeView as? Playable
    }

    private fun defaultAudioBackend(): AudioBackend {
        return audioBackends.first()
    }

}