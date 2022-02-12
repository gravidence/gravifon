package org.gravidence.gravifon.orchestration

import mu.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Orchestrator(private val consumers: List<OrchestratorConsumer>) {

    init {
        logger.info { "Consumer components registered: $consumers" }
    }

    fun startup() {
        logger.info { "Startup routine, started phase 1" }
        consumers.forEach { it.startup() }
        logger.info { "Startup routine, started phase 2" }
        consumers.forEach { it.afterStartup() }
        logger.info { "Startup routine, completed" }
    }

    fun shutdown() {
        logger.info { "Shutdown routine, started" }
        consumers.forEach { it.beforeShutdown() }
        logger.info { "Shutdown routine, completed" }
    }

}