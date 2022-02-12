package org.gravidence.gravifon.plugin.scrobble.lastfm.api

import kotlinx.serialization.json.decodeFromJsonElement
import mu.KotlinLogging
import org.gravidence.gravifon.plugin.scrobble.lastfm.exception.LastfmApiException
import org.gravidence.gravifon.plugin.scrobble.lastfm.exception.LastfmException
import org.gravidence.gravifon.plugin.scrobble.lastfm.misc.*
import org.gravidence.gravifon.plugin.scrobble.lastfm.misc.serialization.lastfmSerializer
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

    @Throws(LastfmException::class)
    private fun call(request: Request): Response {
        val response = httpClient(request).also {
            logger.debug { "Response: $it" }
        }

        if (!response.status.successful) {
            if (response.body.length == null || response.body.length == 0L) {
                throw LastfmException(response.status.toString())
            } else {
                throw LastfmApiException(lastfmSerializer.decodeFromJsonElement(response.toJsonObject()))
            }
        }

        return response
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