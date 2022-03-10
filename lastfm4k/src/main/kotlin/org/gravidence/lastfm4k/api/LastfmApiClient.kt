package org.gravidence.lastfm4k.api

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import mu.KotlinLogging
import org.gravidence.lastfm4k.api.error.ErrorApiResponse
import org.gravidence.lastfm4k.exception.LastfmApiException
import org.gravidence.lastfm4k.exception.LastfmException
import org.gravidence.lastfm4k.exception.LastfmNetworkException
import org.gravidence.lastfm4k.misc.*
import org.gravidence.lastfm4k.resilience.Retry
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.body.form

private val logger = KotlinLogging.logger {}

class LastfmApiClient(
    val apiRoot: String = "http://ws.audioscrobbler.com/2.0/",
    val apiKey: String,
    val apiSecret: String
) {

    private val httpClient: HttpHandler = JavaHttpClient()

    private val retry: Retry = Retry()

    private val extraNetworkErrorCodes = listOf(404, 408, 429)

    @Throws(LastfmException::class)
    private fun call(request: Request): Response {
        return retry.wrap {
            val response = httpClient(request).also {
                logger.debug { "Response: $it" }
            }

            if (!response.status.successful) {
                if (response.header("content-type") == "application/json") {
                    throw LastfmApiException(lastfmSerializer.decodeFromJsonElement(response.toJsonObject()))
                } else if (response.status.serverError || response.status.code in extraNetworkErrorCodes) {
                    throw LastfmNetworkException(response)
                } else {
                    throw LastfmException("Unhandled")
                }
            }

            return@wrap response
        }
    }

    @Throws(LastfmException::class)
    fun get(apiMethod: LastfmApiMethod, params: List<Param?> = listOf()): Response {
        val queries = addCommonParams(apiMethod, params)

        val request = Request(Method.GET, apiRoot)
            .lfmQueryParams(queries)
            .lfmQuerySignature(queries, apiSecret)
            .query("format", "json").also {
                logger.debug { "Request: $it" }
            }

        return call(request)
    }

    @Throws(LastfmException::class)
    fun post(apiMethod: LastfmApiMethod, params: List<Param?> = listOf()): Response {
        val queries = addCommonParams(apiMethod, params)

        val request = Request(Method.POST, apiRoot)
            .lfmFormParams(queries)
            .lfmFormSignature(queries, apiSecret)
            .form("format", "json").also {
                logger.debug { "Request: $it" }
            }

        return call(request)
    }

    private fun addCommonParams(apiMethod: LastfmApiMethod, params: List<Param?> = listOf()): List<Param?> {
        return listOf<Param?>(
            apiMethod.paramMethod(),
            Param("api_key", apiKey)
        ).plus(params)
    }

}

/**
 * Decodes Last.fm API response entity.
 * This method covers case when 200 OK response contains [ErrorApiResponse] body.
 */
@Throws(LastfmException::class)
inline fun <reified T> decodeApiResponse(jsonObject: JsonObject): T {
    try {
        return lastfmSerializer.decodeFromJsonElement(jsonObject)
    } catch (e: Exception) {
        val errorApiResponse = try {
            lastfmSerializer.decodeFromJsonElement<ErrorApiResponse>(jsonObject)
        } catch (ee: Exception) {
            throw LastfmException("Unhandled: ${ee.message}")
        }
        throw LastfmApiException(errorApiResponse)
    }
}