package org.gravidence.lastfm4k

import mu.KotlinLogging
import org.gravidence.lastfm4k.api.LastfmApiClient
import org.gravidence.lastfm4k.api.LastfmApiContext
import org.gravidence.lastfm4k.api.auth.AuthApi
import org.gravidence.lastfm4k.api.auth.Session
import org.gravidence.lastfm4k.api.auth.Token
import org.gravidence.lastfm4k.api.track.TrackApi
import org.gravidence.lastfm4k.exception.LastfmException
import org.http4k.core.*

private val logger = KotlinLogging.logger {}

class LastfmClient(
    val apiRoot: String = "http://ws.audioscrobbler.com/2.0/",
    val apiKey: String,
    val apiSecret: String,
    session: Session? = null
) {

    private val context = LastfmApiContext(
        LastfmApiClient(apiRoot, apiKey, apiSecret),
        session
    )

    val authApi: AuthApi = AuthApi(context)
    val trackApi: TrackApi = TrackApi(context)

    @Throws(LastfmException::class)
    fun authorizeStep1(): Pair<Token, Uri> {
        val token = authApi.getToken()
        val userAuthorizationRequest = authApi.getUserAuthorizationRequest(token)
        return Pair(token, userAuthorizationRequest)
    }

    @Throws(LastfmException::class)
    fun authorizeStep2(token: Token, useNewSession: Boolean = true): Session {
        return authApi.getSession(token).also {
            if (useNewSession) {
                session(it)
            }
        }
    }

    /**
     * Make all APIs use new [session].
     */
    fun session(session: Session?) {
        context.session = session
    }

}