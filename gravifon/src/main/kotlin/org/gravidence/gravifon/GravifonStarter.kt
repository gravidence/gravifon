package org.gravidence.gravifon

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.orchestration.Orchestrator
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playback.backend.AudioBackend
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.util.joinViaSpace
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

private val logger = KotlinLogging.logger {}

private val json = Json { encodeDefaults = true; prettyPrint = true }

object GravifonStarter {

    private val ctx: ApplicationContext

    val orchestrator: Orchestrator
    val configurationManager: ConfigurationManager

    val views: Collection<Viewable>
    val plugins: Collection<Plugin>
    val audioBackends: Collection<AudioBackend>

    init {
        logger.debug {
            """
            |System details:
            |${json.encodeToString(SystemInfo())}
            """
            .trim()
            .trimMargin()
        }

        logger.debug { "Initialize SLF4J bridge handler" }
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        logger.debug { "Initialize Spring application context" }
        ctx = AnnotationConfigApplicationContext()
        ctx.scan("org.gravidence.gravifon")
        ctx.refresh()

        logger.debug { "Expose global beans" }
        orchestrator = ctx.getBean()
        configurationManager = ctx.getBean()
        views = ctx.getBeansOfType<Viewable>().values
        plugins = ctx.getBeansOfType<Plugin>().values
            .sortedBy { it.pluginDisplayName }
        audioBackends = ctx.getBeansOfType<AudioBackend>().values
    }

}

@Serializable
class SystemInfo {

    val os: String = listOf(
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("os.arch")).joinViaSpace()
    val javaRt: String = listOf(
        System.getProperty("java.runtime.name"),
        System.getProperty("java.runtime.version")).joinViaSpace()
    val javaVm: String = listOf(
        System.getProperty("java.vm.name"),
        System.getProperty("java.vm.version")).joinViaSpace()

}