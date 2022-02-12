package org.gravidence.gravifon.plugin.scrobble.lastfm

import mu.KotlinLogging
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.LastfmApiClient
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.auth.AuthApi
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.auth.Session
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.auth.Token
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.track.TrackApi
import org.gravidence.gravifon.plugin.scrobble.lastfm.exception.LastfmException
import org.http4k.core.*

private val logger = KotlinLogging.logger {}

class LastfmClient(
    val apiRoot: String = "http://ws.audioscrobbler.com/2.0/",
    val apiKey: String,
    val apiSecret: String,
    val session: Session? = null
) {

    private val apiClient: LastfmApiClient = LastfmApiClient(apiRoot, apiKey, apiSecret)

    val authApi: AuthApi = AuthApi(session, apiClient)
    val trackApi: TrackApi = TrackApi(session, apiClient)

    @Throws(LastfmException::class)
    fun authorizeStep1(): Pair<Token, Uri> {
        val token = authApi.getToken()
        val userAuthorizationRequest = authApi.getUserAuthorizationRequest(token)
        return Pair(token, userAuthorizationRequest)
    }

    @Throws(LastfmException::class)
    fun authorizeStep2(token: Token): Session {
        return authApi.getSession(token)
    }

}