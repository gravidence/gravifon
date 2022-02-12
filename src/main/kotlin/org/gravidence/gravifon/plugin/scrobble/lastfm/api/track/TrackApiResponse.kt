package org.gravidence.gravifon.plugin.scrobble.lastfm.api.track

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class NowPlayingResponse(
    @SerialName("nowplaying")
    val result: ScrobbleResult
)

@Serializable
class ScrobbleResponse(
    @SerialName("scrobbles")
    val responseHolder: ScrobbleResponseHolder
)

@Serializable
class ScrobbleResponseHolder(
    @SerialName("scrobble")
    val result: ScrobbleResult,
    @SerialName("@attr")
    val summary: ScrobbleSummary
)

@Serializable
class BatchScrobbleResponse(
    @SerialName("scrobbles")
    val responseHolder: BatchScrobbleResponseHolder
)

@Serializable
class BatchScrobbleResponseHolder(
    @SerialName("scrobble")
    val results: List<ScrobbleResult>,
    @SerialName("@attr")
    val summary: ScrobbleSummary
)

@Serializable
class ScrobbleResult(
    val timestamp: Long? = null,
    @SerialName("ignoredMessage")
    val scrobbleCorrectionSummary: ScrobbleCorrectionSummary,
    @SerialName("track")
    val trackCorrectionSummary: ParamCorrectionSummary,
    @SerialName("artist")
    val artistCorrectionSummary: ParamCorrectionSummary,
    @SerialName("album")
    val albumCorrectionSummary: ParamCorrectionSummary,
    @SerialName("albumArtist")
    val albumArtistCorrectionSummary: ParamCorrectionSummary,
)

@Serializable(with = IgnoreStatus.IgnoreStatusAsStringSerializer::class)
enum class IgnoreStatus(val code: Int) {

    OK(0),
    FILTERED_ARTIST(1),
    FILTERED_TRACK(2),
    TIMESTAMP_IN_THE_PAST(3),
    TIMESTAMP_IN_THE_FUTURE(4),
    DAILY_SCROBBLE_LIMIT_EXCEEDED(5);

    companion object {
        fun valueOfCode(code: String): IgnoreStatus {
            return values().first { it.code == code.toInt() }
        }
    }

    object IgnoreStatusAsStringSerializer : KSerializer<IgnoreStatus> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("IgnoreStatus", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): IgnoreStatus {
            return valueOfCode(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: IgnoreStatus) {
            encoder.encodeString(value.code.toString())
        }

    }

}

@Serializable
class ScrobbleCorrectionSummary(
    @SerialName("code")
    val status: IgnoreStatus,
    @SerialName("#text")
    val value: String
)

@Serializable(with = CorrectionStatus.CorrectionStatusAsStringSerializer::class)
enum class CorrectionStatus(val code: Int) {

    UNCHANGED(0),
    CORRECTED(1);

    companion object {
        fun valueOfCode(code: String): CorrectionStatus {
            return values().first { it.code == code.toInt() }
        }
    }

    object CorrectionStatusAsStringSerializer : KSerializer<CorrectionStatus> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("CorrectionStatus", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): CorrectionStatus {
            return valueOfCode(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: CorrectionStatus) {
            encoder.encodeString(value.code.toString())
        }

    }

}

@Serializable
class ParamCorrectionSummary(
    @SerialName("corrected")
    val status: CorrectionStatus,
    @SerialName("#text")
    val value: String
)

@Serializable
class ScrobbleSummary(
    val accepted: Int,
    val ignored: Int
)

fun ScrobbleResponse.toBatchScrobbleResponse(): BatchScrobbleResponse {
    return BatchScrobbleResponse(BatchScrobbleResponseHolder(
        results = listOf(responseHolder.result),
        summary = responseHolder.summary
    ))
}