package org.gravidence.gravifon.event

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

abstract class EventConsumer(subscribe: ((Event) -> Unit) -> Unit = EventBus::subscribe) {

    init {
        subscribe(::receive)
    }

    private fun receive(event: Event) {
        logger.trace { "$event received by $this" }
        consume(event)
    }

    abstract fun consume(event: Event)

}