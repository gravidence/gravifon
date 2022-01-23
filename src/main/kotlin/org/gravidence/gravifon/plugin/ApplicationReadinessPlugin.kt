package org.gravidence.gravifon.plugin

import mu.KotlinLogging
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PubApplicationReadyEvent
import org.gravidence.gravifon.event.component.PubLibraryReadyEvent
import org.gravidence.gravifon.event.component.PubPlaylistManagerReadyEvent
import org.gravidence.gravifon.event.component.PubSettingsReadyEvent
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

private val logger = KotlinLogging.logger {}

@Component
class ApplicationReadinessPlugin : Plugin() {

    private val checks: MutableMap<KClass<out Event>, Boolean> = mutableMapOf(
        Pair(PubLibraryReadyEvent::class, false),
        Pair(PubPlaylistManagerReadyEvent::class, false),
        Pair(PubSettingsReadyEvent::class, false),
    )
    private var jobDone: Boolean = false

    override fun consume(event: Event) {
        if (!jobDone && checkReadiness(event).also { jobDone = it }) {
            publish(PubApplicationReadyEvent()).also {
                logger.info { "All components successfully initialized" }
                logger.debug { "Following readiness events received ${checks.keys.map { it.simpleName }}" }
            }
        }
    }

    @Synchronized
    private fun checkReadiness(event: Event): Boolean {
        if (checks.containsKey(event::class) && checks[event::class] != true) {
            checks[event::class] = true
        }
        return checks.all { it.value }
    }

}