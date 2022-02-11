package org.gravidence.gravifon.plugin.scrobble.lastfm.api.track

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val ignoredMessage: ScrobbleCorrectionSummary,
    val track: ParamCorrectionSummary,
    val artist: ParamCorrectionSummary,
    val album: ParamCorrectionSummary,
    val albumArtist: ParamCorrectionSummary,
)



@Serializable
class ScrobbleCorrectionSummary(
    val code: Int,
    @SerialName("#text")
    val value: String
)

@Serializable
class ParamCorrectionSummary(
    @SerialName("corrected")
    val status: Int,
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