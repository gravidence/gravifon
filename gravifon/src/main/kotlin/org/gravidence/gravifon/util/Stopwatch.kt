package org.gravidence.gravifon.util

import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.TimeSource

private val logger = KotlinLogging.logger {}

class Stopwatch {

    private val timeSource = TimeSource.Monotonic

    private var spanStart: TimeSource.Monotonic.ValueTimeMark? = null

    private var elapsed: Duration = Duration.ZERO

    @Synchronized
    fun count() {
        // start new cycle only if measurement is NOT in progress already
        if (spanStart == null) {
            spanStart = timeSource.markNow().also { start ->
                logger.trace { "Open stopwatch span at $start" }
            }
        }
    }

    @Synchronized
    fun pause(): Duration {
        // update state only if measurement is in progress
        spanStart?.let { start ->
            // update elapsed counter
            val spanEnd = timeSource.markNow().also { end ->
                logger.trace { "Close stopwatch span at $end" }
            }
            elapsed += spanEnd - start

            // reset span to mark end of a measurement cycle
            spanStart = null
        }

        return elapsed()
    }

    @Synchronized
    fun stop(): Duration {
        // finish up measurement process and keep total duration
        val duration = pause()

        // reset elapsed counter
        elapsed = Duration.ZERO

        return duration
    }

    private fun elapsed(): Duration {
        return elapsed.also {
            logger.trace { "Elapsed time is $it" }
        }
    }

}