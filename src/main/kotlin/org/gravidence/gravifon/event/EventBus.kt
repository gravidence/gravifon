package org.gravidence.gravifon.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.gravidence.gravifon.Gravifon.scopeDefault

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
            events.collect {
                try {
                    receive(it)
                } catch (e: Exception) {
                    logger.error(e) { "Internal application error occurred while handling ${it.javaClass.simpleName} event" }
                }
            }
        }
    }

}