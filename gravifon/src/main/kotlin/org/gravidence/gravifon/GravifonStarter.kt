package org.gravidence.gravifon

import mu.KotlinLogging
import org.gravidence.gravifon.orchestration.Orchestrator
import org.gravidence.gravifon.playback.Player
import org.gravidence.gravifon.plugin.library.Library
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

private val logger = KotlinLogging.logger {}

object GravifonStarter {

    val ctx: ApplicationContext

    val player: Player
    val library: Library
    val orchestrator: Orchestrator

    init {
        logger.debug { "Initialize SLF4J bridge handler" }
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        logger.debug { "Initialize Spring application context" }
        ctx = AnnotationConfigApplicationContext()
        ctx.scan("org.gravidence.gravifon")
        ctx.refresh()

        // TODO these shouldn't be needed when time comes?
        logger.debug { "Expose global beans" }
        player = ctx.getBean<Player>()
        library = ctx.getBean<Library>()
        orchestrator = ctx.getBean<Orchestrator>()
    }

    fun kickoff() {
        logger.info { "Spin-up application" }
        orchestrator.startup()
    }

}