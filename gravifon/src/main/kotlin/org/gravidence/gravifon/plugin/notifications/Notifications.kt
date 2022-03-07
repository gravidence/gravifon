package org.gravidence.gravifon.plugin.notifications

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.domain.notification.NotificationLifespan
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PushInnerNotificationEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Notifications : EventAware {

    private val innerNotificationChannel = Channel<Notification>(Channel.CONFLATED)

    init {
        handleInnerNotifications()
    }

    override fun consume(event: Event) {
        when (event) {
            is PushInnerNotificationEvent -> {
                innerNotificationChannel.trySend(event.notification)
            }
        }
    }

    private fun handleInnerNotifications() = GravifonContext.scopeDefault.launch {
        innerNotificationChannel.receiveAsFlow()
            .onEach {
                logger.trace { "Process inner notification: $it" }

                GravifonContext.activeInnerNotification.value = it

                delay(
                    if (it.lifespan == NotificationLifespan.INFINITE) {
                        NotificationLifespan.LONG.value
                    } else {
                        it.lifespan.value
                    }.also {
                        logger.trace { "Notification will stay for ${it.inWholeMilliseconds}ms" }
                    }
                )

                if (it.lifespan != NotificationLifespan.INFINITE) {
                    GravifonContext.activeInnerNotification.value = null
                }
            }
            .collect()
    }

}