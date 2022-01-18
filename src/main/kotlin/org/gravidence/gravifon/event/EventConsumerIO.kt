package org.gravidence.gravifon.event

abstract class EventConsumerIO: EventConsumer() {

    override fun subscribe() {
        EventBus.subscribeIO(::receive)
    }

}