package org.gravidence.gravifon.domain.notification

data class Notification(
    val message: String,
    val type: NotificationType = NotificationType.REGULAR,
    val lifespan: NotificationLifespan = NotificationLifespan.MEDIUM,
)