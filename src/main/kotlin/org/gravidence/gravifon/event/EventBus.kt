package org.gravidence.gravifon.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.gravidence.gravifon.Gravifon.scopeDefault
import org.gravidence.gravifon.Gravifon.scopeIO

private val logger = KotlinLogging.logger {}

object EventBus {

    private val events =
        MutableSharedFlow<Event>(extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun publish(event: Event) {
        scopeDefault.launch {
            logger.trace { "$event published" }
            events.emit(event)
        }
    }

    fun subscribe(receive: (Event) -> Unit) {
        scopeDefault.launch {
            events.collect { receive(it) }
        }
    }

    fun subscribeIO(receive: (Event) -> Unit) {
        scopeIO.launch {
            events.collect { receive(it) }
        }
    }

}