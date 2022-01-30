package org.gravidence.gravifon.event

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext

private val logger = KotlinLogging.logger {}

object EventBus {

    private val events =
        MutableSharedFlow<Event>(extraBufferCapacity = 100, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    fun publish(event: Event) {
        GravifonContext.scopeDefault.launch {
            logger.trace { "$event published" }
            events.emit(event)
        }
    }

    fun subscribe(receive: (Event) -> Unit) {
        GravifonContext.scopeDefault.launch {
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