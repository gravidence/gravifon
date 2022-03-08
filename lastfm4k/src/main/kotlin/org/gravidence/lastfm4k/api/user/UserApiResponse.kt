package org.gravidence.lastfm4k.api.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.lastfm4k.misc.BooleanAsIntSerializer

@Serializable
class UserInfoResponse(
    @SerialName("user")
    val userInfo: UserInfo
)

@Serializable
class UserInfo(
    @SerialName("type")
    val type: String,
    @SerialName("name")
    val name: String,
    @SerialName("realname")
    val realname: String,
    @SerialName("url")
    val url: String,
//    val image: String,
    @SerialName("country")
    val country: String,
    @SerialName("age")
    val age: Int,
//    val gender: String,
    @SerialName("subscriber")
    @Serializable(with = BooleanAsIntSerializer::class)
    val subscriber: Boolean,
    @SerialName("playcount")
    val playcount: Long,
    @SerialName("playlists")
    val playlists: Int,
    @SerialName("bootstrap")
    val bootstrap: Int,
//    val registered: String,
)