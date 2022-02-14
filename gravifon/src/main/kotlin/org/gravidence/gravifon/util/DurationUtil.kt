package org.gravidence.gravifon.util

import kotlin.time.Duration

object DurationUtil {

    /**
     * Formats [duration] according to <h>:mm:ss (where hours are added if present only).
     */
    fun format(duration: Duration?): String {
        return duration?.toComponents { hours, minutes, seconds, nanoseconds ->
            val builder = StringBuilder()
            if (hours > 0) {
                builder.append("$hours:")
            }
            builder.append("${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}")
            builder.toString()
        } ?: "--:--"
    }

    fun max(a: Duration, b: Duration): Duration {
        return if (a >= b) a else b
    }

}