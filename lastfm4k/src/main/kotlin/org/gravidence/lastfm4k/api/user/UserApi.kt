package org.gravidence.lastfm4k.api.user

import org.gravidence.lastfm4k.api.LastfmApiContext
import org.gravidence.lastfm4k.api.LastfmApiMethod
import org.gravidence.lastfm4k.api.decodeApiResponse
import org.gravidence.lastfm4k.api.paramSessionKey
import org.gravidence.lastfm4k.misc.Param
import org.gravidence.lastfm4k.misc.toJsonObject

class UserApi(private val context: LastfmApiContext) {

    /**
     * Fetches specific [user] info. Defaults to the authenticated user.
     */
    fun getInfo(user: String? = null): UserInfoResponse {
        val response = context.client.get(
            LastfmApiMethod.USER_GETINFO,
            listOf(
                user?.let { Param("user", it) } ?: context.session?.paramSessionKey(),
            )
        )

        return decodeApiResponse(response.toJsonObject())
    }

}