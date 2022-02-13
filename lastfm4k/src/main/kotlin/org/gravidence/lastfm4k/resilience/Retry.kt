package org.gravidence.lastfm4k.resilience

import kotlinx.datetime.*
import mu.KotlinLogging
import org.gravidence.lastfm4k.api.error.LastfmApiError
import org.gravidence.lastfm4k.exception.LastfmApiException
import org.gravidence.lastfm4k.exception.LastfmException
import org.gravidence.lastfm4k.exception.LastfmNetworkException
import org.gravidence.lastfm4k.misc.toLocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private val logger = KotlinLogging.logger {}

class Retry(
    val minWaitDuration: Duration = 1.minutes,
    val waitDurationMultiplier: Double = 2.toDouble(),
    val maxWaitDuration: Duration = 8.hours,
    val maxAttempts: Int = Int.MAX_VALUE
) {

    private var retryAfter: Instant? = null

    private var currentWaitDuration: Duration? = null
    private var currentAttempt: Int? = null

    @Synchronized
    fun <V> wrap(block: () -> V): V {
        val retryAfterFixed = retryAfter

        if (retryAfterFixed == null || Clock.System.now() >= retryAfterFixed) {
            try {
                return block()
                    .also { success() }
            } catch (exc: LastfmApiException) {
                when (exc.response.error) {
                    LastfmApiError.SERVICE_OFFLINE, LastfmApiError.TEMPORARY_UNAVAILABLE -> {
                        failure()
                    }
                    else -> {
                        logger.warn(exc) { "Not entering retry flow, because it's likely a client side error" }
                    }
                }
                throw exc
            } catch (exc: LastfmNetworkException) {
                failure()
                throw exc
            }
        } else {
            throw LastfmException("Skip Last.fm service call, next retry after ${retryAfterFixed.toLocalDateTime()}")
        }
    }

    private fun success() {
        retryAfter = null
        currentWaitDuration = null
        currentAttempt = null
    }

    private fun failure() {
        val nextWaitDuration = currentWaitDuration?.times(waitDurationMultiplier) ?: minWaitDuration
        val nextAttempt = currentAttempt?.plus(1) ?: 1

        if (nextWaitDuration < maxWaitDuration && nextAttempt < maxAttempts) {
            currentWaitDuration = nextWaitDuration
            currentAttempt = nextAttempt
        }

        retryAfter = Clock.System.now().plus(nextWaitDuration).also {
            logger.info { "Failed to to call Last.fm service, next retry after ${it.toLocalDateTime()}" }
        }
    }

}