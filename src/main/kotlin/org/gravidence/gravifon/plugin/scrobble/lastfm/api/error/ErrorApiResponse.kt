package org.gravidence.gravifon.plugin.scrobble.lastfm.api.error

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = LastfmApiError.AsIntSerializer::class)
enum class LastfmApiError(val code: Int) {

    INVALID_SERVICE(2),
    INVALID_METHOD(3),
    AUTHENTICATION_FAILED(4),
    INVALID_FORMAT(5),
    INVALID_PARAMETERS(6),
    INVALID_RESOURCE(7),
    OPERATION_FAILED(8),
    INVALID_SESSION_KEY(9),
    INVALID_API_KEY(10),
    SERVICE_OFFLINE(11),
    INVALID_METHOD_SIGNATURE(13),
    TEMPORARY_UNAVAILABLE(16),
    SUSPENDED_API_KEY(26),
    RATE_LIMIT_EXCEEDED(29);

    companion object {
        fun valueOfCode(code: Int): LastfmApiError {
            return values().first { it.code == code }
        }
    }

    object AsIntSerializer : KSerializer<LastfmApiError> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LastfmApiError", PrimitiveKind.INT)

        override fun deserialize(decoder: Decoder): LastfmApiError {
            return valueOfCode(decoder.decodeInt())
        }

        override fun serialize(encoder: Encoder, value: LastfmApiError) {
            encoder.encodeInt(value.code)
        }

    }

}

@Serializable
data class ErrorApiResponse(val error: LastfmApiError, val message: String)
