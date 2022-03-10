package org.gravidence.lastfm4k.api.auth

import mu.KotlinLogging
import org.gravidence.lastfm4k.api.LastfmApiContext
import org.gravidence.lastfm4k.api.LastfmApiMethod
import org.gravidence.lastfm4k.api.decodeApiResponse
import org.gravidence.lastfm4k.api.paramToken
import org.gravidence.lastfm4k.misc.Param
import org.gravidence.lastfm4k.misc.lfmQueryParams
import org.gravidence.lastfm4k.misc.toJsonObject
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Uri

private val logger = KotlinLogging.logger {}

class AuthApi(private val context: LastfmApiContext) {

    private var token: Token? = null

    fun getToken(): Token {
        return token ?: refreshToken()
    }

    fun getUserAuthorizationRequest(token: Token): Uri {
        val request = Request(Method.GET, "http://www.last.fm/api/auth/")
            .lfmQueryParams(
                listOf(
                    Param("api_key", context.client.apiKey),
                    token.paramToken()
                )
            ).also {
                logger.debug { "User authorization request: $it" }
            }

        return request.uri
    }

    fun getSession(token: Token): Session {
        return context.session ?: refreshSession(token)
    }

    private fun refreshToken(): Token {
        val response = context.client.get(LastfmApiMethod.AUTH_GETTOKEN)

        return decodeApiResponse(response.toJsonObject())
    }

    private fun refreshSession(token: Token): Session {
        val response = context.client.get(
            LastfmApiMethod.AUTH_GETSESSION,
            listOf(
                token.paramToken()
            )
        )

        return decodeApiResponse<SessionResponse>(response.toJsonObject())
            .session
    }

}