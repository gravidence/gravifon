package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.playback.PlaybackStatus
import org.gravidence.gravifon.util.DurationUtil
import kotlin.time.Duration

class PlaybackInformationState(
    val playbackStatus: PlaybackStatus,
    val artistInformation: String,
    val trackInformation: String,
    val trackExtraInformation: String,
    val albumInformation: String,
) {

    companion object {

        fun renderArtistInformation(track: VirtualTrack?): String {
            return track?.getArtist() ?: "<unknown artist>"
        }

        fun renderTrackInformation(track: VirtualTrack?): String {
            return track?.getTitle() ?: "<unknown title>"
        }

        fun renderTrackExtraInformation(trackLength: Duration): String {
            return "(" + DurationUtil.formatShortHours(trackLength) + ")"
        }

        fun renderAlbumInformation(track: VirtualTrack?): String {
            val album = track?.getAlbum() ?: "<unknown album>"
            val date = track?.getDate()
            val albumArtist = track?.getAlbumArtist()

            val builder = StringBuilder(album)
            if (date != null) {
                builder.append(" ($date)")
            }
            if (albumArtist != null) {
                builder.append(" by $albumArtist")
            }
            return builder.toString()
        }

    }

}

@Composable
fun rememberPlaybackInformationState(
    activeTrack: VirtualTrack? = GravifonContext.activeTrack.value,
    playbackStatus: PlaybackStatus = GravifonContext.playbackStatusState.value,
    trackLength: Duration = GravifonContext.playbackPositionState.value.endingPosition,
) = remember(activeTrack, playbackStatus, trackLength) {
    PlaybackInformationState(
        playbackStatus = playbackStatus,
        artistInformation = PlaybackInformationState.renderArtistInformation(activeTrack),
        trackInformation = PlaybackInformationState.renderTrackInformation(activeTrack),
        trackExtraInformation = PlaybackInformationState.renderTrackExtraInformation(trackLength),
        albumInformation = PlaybackInformationState.renderAlbumInformation(activeTrack),
    )
}

@Composable
fun PlaybackInformationComposable(playbackInformationState: PlaybackInformationState) {
    Box(
        modifier = Modifier
            .height(90.dp)
            .padding(5.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            if (playbackInformationState.playbackStatus == PlaybackStatus.STOPPED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Text(text = "Idle...", fontWeight = FontWeight.ExtraLight)
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(text = playbackInformationState.artistInformation, fontWeight = FontWeight.ExtraBold)
                }
                Divider(color = Color.Transparent, thickness = 1.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(text = playbackInformationState.trackInformation, fontWeight = FontWeight.Bold)
                    Text(text = playbackInformationState.trackExtraInformation, fontWeight = FontWeight.Light)
                }
                Divider(color = Color.Transparent, thickness = 1.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(text = playbackInformationState.albumInformation, fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}