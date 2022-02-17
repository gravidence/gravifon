package org.gravidence.gravifon.event

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

abstract class EventHandler {

    val publish: (Event) -> Unit = EventBus::publish

    init {
        EventBus.subscribe(::receive)
    }

    private fun receive(event: Event) {
        logger.trace { "$event received by $this" }
        consume(event)
    }

    abstract fun consume(event: Event)

}