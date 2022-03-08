package org.gravidence.lastfm4k.api.user

import kotlinx.serialization.json.decodeFromJsonElement
import org.gravidence.lastfm4k.api.LastfmApiContext
import org.gravidence.lastfm4k.api.LastfmApiMethod
import org.gravidence.lastfm4k.api.paramSessionKey
import org.gravidence.lastfm4k.misc.lastfmSerializer
import org.gravidence.lastfm4k.misc.toJsonObject

class UserApi(private val context: LastfmApiContext) {

    fun getInfo(): UserInfoResponse {
        val response = context.client.get(
            LastfmApiMethod.USER_GETINFO,
            listOf(
                context.session?.paramSessionKey(),
            )
        )

        return lastfmSerializer.decodeFromJsonElement(response.toJsonObject())
    }

}