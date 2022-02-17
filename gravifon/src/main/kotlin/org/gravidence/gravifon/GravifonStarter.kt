package org.gravidence.gravifon

import mu.KotlinLogging
import org.gravidence.gravifon.orchestration.Orchestrator
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.plugin.View
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

private val logger = KotlinLogging.logger {}

object GravifonStarter {

    private val ctx: ApplicationContext

    val orchestrator: Orchestrator

    val views: Collection<View>
    val plugins: Collection<Plugin>

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
        views = ctx.getBeansOfType<View>().values.also {
            logger.info { "Views configured: $it" }
        }
        plugins = ctx.getBeansOfType<Plugin>().values
            .sortedBy { it.title }
            .also { logger.info { "Plugins configured: $it" } }
    }

}