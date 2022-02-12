package org.gravidence.gravifon.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import mu.KotlinLogging
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

class Stopwatch {

    private var elapsed: Duration = Duration.ZERO

    private var spanStart: Instant? = null

    @Synchronized
    fun count() {
        // start new cycle only if measurement is NOT in progress already
        if (spanStart == null) {
            spanStart = Clock.System.now().also {
                logger.trace { "Open stopwatch span at $it" }
            }
        }
    }

    @Synchronized
    fun pause(): Duration {
        // update state only if measurement is in progress
        spanStart?.let { start ->
            // update elapsed counter
            val spanEnd = Clock.System.now().also { end ->
                logger.trace { "Close stopwatch span at $end" }
            }
            elapsed += spanEnd.minus(start)

            // reset span to mark end of a measurement cycle
            spanStart = null
        }

        return elapsed()
    }

    @Synchronized
    fun stop(): Duration {
        // finish up measurement process and keep keep total duration
        val duration = pause()

        // reset elapsed counter
        elapsed = Duration.ZERO

        return duration
    }

    fun elapsed(): Duration {
        return elapsed.also {
            logger.trace { "Elapsed time is $it" }
        }
    }

}