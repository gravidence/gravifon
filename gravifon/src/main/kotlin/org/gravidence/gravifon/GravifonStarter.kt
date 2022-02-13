package org.gravidence.gravifon

import mu.KotlinLogging
import org.gravidence.gravifon.orchestration.Orchestrator
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

private val logger = KotlinLogging.logger {}

object GravifonStarter {

    private val ctx: ApplicationContext

    val orchestrator: Orchestrator

    init {
        logger.debug { "Initialize SLF4J bridge handler" }
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        logger.debug { "Initialize Spring application context" }
        ctx = AnnotationConfigApplicationContext()
        ctx.scan("org.gravidence.gravifon")
        ctx.refresh()

        logger.debug { "Expose global beans" }
        orchestrator = ctx.getBean()
    }

}