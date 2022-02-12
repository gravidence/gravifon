package org.gravidence.lastfm4k.api.auth

import kotlinx.serialization.json.decodeFromJsonElement
import mu.KotlinLogging
import org.gravidence.lastfm4k.api.LastfmApiClient
import org.gravidence.lastfm4k.api.LastfmApiMethod
import org.gravidence.lastfm4k.api.paramToken
import org.gravidence.lastfm4k.misc.Param
import org.gravidence.lastfm4k.misc.lastfmSerializer
import org.gravidence.lastfm4k.misc.lfmQueryParams
import org.gravidence.lastfm4k.misc.toJsonObject
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri

private val logger = KotlinLogging.logger {}

class AuthApi(private var session: Session? = null, private val client: LastfmApiClient) {

    private var token: Token? = null

    fun getToken(): Token {
        return token ?: refreshToken()
    }

    fun getUserAuthorizationRequest(token: Token): Uri {
        val request = Request(Method.GET, "http://www.last.fm/api/auth/")
            .lfmQueryParams(
                listOf(
                    Param("api_key", client.apiKey),
                    token.paramToken()
                )
            ).also {
                logger.debug { "User authorization request: $it" }
            }

        return request.uri
    }

    fun getSession(token: Token): Session {
        return session ?: refreshSession(token)
    }

    private fun refreshToken(): Token {
        val response = client.get(LastfmApiMethod.AUTH_GETTOKEN)

        return lastfmSerializer.decodeFromJsonElement(response.toJsonObject())
    }

    private fun refreshSession(token: Token): Session {
        val response = client.get(
            LastfmApiMethod.AUTH_GETSESSION,
            listOf(
                token.paramToken()
            )
        )

        return lastfmSerializer.decodeFromJsonElement<SessionResponse>(response.toJsonObject())
            .session
    }

}