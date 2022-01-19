package org.gravidence.gravifon.event

abstract class EventConsumer {

    init {
        subscribe()
    }

    protected open fun subscribe() {
        EventBus.subscribe(::receive)
    }

    protected fun receive(event: Event) {
//        println("$event received by $this")
        consume(event)
    }

    abstract fun consume(event: Event)

}