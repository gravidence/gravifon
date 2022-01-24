package org.gravidence.gravifon.orchestration

import mu.KotlinLogging
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventHandler
import org.gravidence.gravifon.event.application.SubApplicationShutdownEvent
import org.gravidence.gravifon.event.application.SubApplicationStartupEvent
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Orchestrator(private val consumers: List<OrchestratorConsumer>) : EventHandler() {

    init {
        logger.info { "Consumer components registered: $consumers" }
    }

    override fun consume(event: Event) {
        when (event) {
            is SubApplicationStartupEvent -> {
                logger.info { "Startup routine, started phase 1" }
                consumers.forEach { it.boot() }
                logger.info { "Startup routine, started phase 2" }
                consumers.forEach { it.afterStartup() }
                logger.info { "Startup routine, completed" }
            }
            is SubApplicationShutdownEvent -> {
                logger.info { "Shutdown routine, started" }
                consumers.forEach { it.beforeShutdown() }
                logger.info { "Shutdown routine, completed" }
            }
        }
    }

}