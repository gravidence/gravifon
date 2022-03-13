package org.gravidence.gravifon.util

import kotlin.time.Duration

object DurationUtil {

    /**
     * Formats [duration] according to <h>:mm:ss (where hours are added if present only).
     */
    fun formatShortHours(duration: Duration?): String {
        return duration?.toComponents { hours, minutes, seconds, _ ->
            val builder = StringBuilder()
            if (hours > 0) {
                builder.append("$hours:")
            }
            builder.append("${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}")
            builder.toString()
        } ?: "--:--"
    }

    /**
     * Formats [duration] to 1h 15m 7s (where hours are omitted if 0).
     */
    fun formatLongHours(duration: Duration): String {
        return duration.toComponents { hours, minutes, seconds, _ ->
            val builder = StringBuilder()
            if (hours > 0) {
                builder.append("${hours}h ")
            }
            builder.append("${minutes}m ${seconds}s")
            builder.toString()
        }
    }

    /**
     * Formats [duration] to 1d 15h 7m (where days are omitted if 0).
     */
    fun formatLongDays(duration: Duration): String {
        return duration.toComponents { days, hours, minutes, _, _ ->
            val builder = StringBuilder()
            if (days > 0) {
                builder.append("${days}d ")
            }
            builder.append("${hours}h ${minutes}m")
            builder.toString()
        }
    }

    fun max(a: Duration, b: Duration): Duration {
        return if (a >= b) a else b
    }

}