package org.gravidence.gravifon.orchestration.marker

import mu.KotlinLogging
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventBus

private val logger = KotlinLogging.logger {}

interface EventAware {

    fun publish(event: Event) {
        EventBus.publish(event)
    }

    fun receive(event: Event) {
        logger.trace { "$event received by $this" }
        consume(event)
    }

    fun consume(event: Event)

}