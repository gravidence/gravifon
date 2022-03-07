package org.gravidence.gravifon.domain.notification

import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

enum class NotificationLifespan(val value: Duration) {

    SHORT(100.toDuration(DurationUnit.MILLISECONDS)),
    MEDIUM(1.toDuration(DurationUnit.SECONDS)),
    LONG(5.toDuration(DurationUnit.SECONDS)),
    INFINITE(Duration.INFINITE),

}
