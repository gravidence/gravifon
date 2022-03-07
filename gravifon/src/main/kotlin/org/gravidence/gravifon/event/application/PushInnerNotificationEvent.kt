package org.gravidence.gravifon.event.application

import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.event.Event

/**
 * Emitted when a component wants to send an inner notification.
 */
class PushInnerNotificationEvent(val notification: Notification): Event