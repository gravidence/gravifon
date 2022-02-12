package org.gravidence.gravifon.plugin.scrobble.lastfm.api.track

import kotlinx.serialization.json.decodeFromJsonElement
import mu.KotlinLogging
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.*
import org.gravidence.gravifon.plugin.scrobble.lastfm.api.auth.Session
import org.gravidence.gravifon.plugin.scrobble.lastfm.misc.serialization.lastfmSerializer
import org.gravidence.gravifon.plugin.scrobble.lastfm.misc.toJsonObject

private val logger = KotlinLogging.logger {}

class TrackApi(private var session: Session?, private val client: LastfmApiClient) {

    fun updateNowPlaying(track: Track): NowPlayingResponse {
        val response = client.post(LastfmApiMethod.TRACK_UPDATENOWPLAYING,
            listOf(
                session?.paramSessionKey(),
                track.paramArtist(),
                track.paramTrack(),
                track.paramAlbum(),
                track.paramAlbumArtist(),
                track.paramTrackNumber(),
                track.paramMbId(),
                track.paramDuration(),
            )
        )

        return lastfmSerializer.decodeFromJsonElement(response.toJsonObject())
    }

    fun scrobble(scrobbles: List<Scrobble>): BatchScrobbleResponse {
        val scrobbleParams = scrobbles.flatMapIndexed { index: Int, scrobble: Scrobble ->
            listOf(
                scrobble.track.paramArtist(index),
                scrobble.track.paramTrack(index),
                scrobble.track.paramAlbum(index),
                scrobble.track.paramAlbumArtist(index),
                scrobble.track.paramTrackNumber(index),
                scrobble.track.paramMbId(index),
                scrobble.track.paramDuration(index),
                scrobble.paramTimestamp(index)
            )
        }

        val response = client.post(LastfmApiMethod.TRACK_SCROBBLE,
            scrobbleParams + session?.paramSessionKey()
        )

        return if (scrobbles.size == 1) {
            lastfmSerializer.decodeFromJsonElement<ScrobbleResponse>(response.toJsonObject())
                .toBatchScrobbleResponse()
        } else {
            lastfmSerializer.decodeFromJsonElement<BatchScrobbleResponse>(response.toJsonObject())
        }
    }

}