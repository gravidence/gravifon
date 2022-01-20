package org.gravidence.gravifon.event

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

abstract class EventConsumer {

    init {
        subscribe()
    }

    protected open fun subscribe() {
        EventBus.subscribe(::receive)
    }

    protected fun receive(event: Event) {
        logger.trace { "$event received by $this" }
        consume(event)
    }

    abstract fun consume(event: Event)

}