package org.gravidence.lastfm4k.api.user

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.gravidence.lastfm4k.misc.BooleanAsIntSerializer
import org.gravidence.lastfm4k.misc.InstantAsUnixTimeStringSerializer

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
    @SerialName("image")
    val images: List<Image>,
    @SerialName("country")
    val country: String,
    @SerialName("age")
    val age: Int,
    @SerialName("gender")
    val gender: String, // possible values are f/m/n?
    @SerialName("subscriber")
    @Serializable(with = BooleanAsIntSerializer::class)
    val subscriber: Boolean,
    @SerialName("playcount")
    val playcount: Long,
    @SerialName("playlists")
    val playlists: Int,
    @SerialName("bootstrap")
    val bootstrap: Int,
    @SerialName("registered")
    val registrationInfo: RegistrationInfo,
)

@Serializable
class RegistrationInfo(
    @SerialName("unixtime")
    @Serializable(with = InstantAsUnixTimeStringSerializer::class)
    val timestamp: Instant,
)

@Serializable
class Image(
    @SerialName("size")
    val size: ImageSize,
    @SerialName("#text")
    val url: String,
)

@Serializable(with = ImageSize.AsStringSerializer::class)
enum class ImageSize(val description: String) {

    S("small"),
    M("medium"),
    L("large"),
    XL("extralarge");

    companion object {
        fun valueOfDescription(description: String): ImageSize {
            return values().first { it.description == description }
        }
    }

    object AsStringSerializer : KSerializer<ImageSize> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ImageSize", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): ImageSize {
            return valueOfDescription(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: ImageSize) {
            encoder.encodeString(value.description)
        }

    }

}