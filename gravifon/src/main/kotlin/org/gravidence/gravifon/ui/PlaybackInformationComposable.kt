package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.playback.PlaybackState
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.util.DurationUtil

class PlaybackInformationState(
    val activeVirtualTrack: MutableState<VirtualTrack?>,
    val playbackState: MutableState<PlaybackState>,
    val playbackPositionState: MutableState<PlaybackPositionState>
) {

    fun renderArtistInformation(): String {
        return activeVirtualTrack.value?.getArtist() ?: "<unknown artist>"
    }

    fun renderTrackInformation(): String {
        return activeVirtualTrack.value?.getTitle() ?: "<unknown title>"
    }

    fun renderTrackExtraInformation(): String {
        return "(" + DurationUtil.format(playbackPositionState.value.endingPosition) + ")"
    }

    fun renderAlbumInformation(): String {
        val album = activeVirtualTrack.value?.getAlbum() ?: "<unknown album>"
        val date = activeVirtualTrack.value?.getDate()
        val albumArtist = activeVirtualTrack.value?.getAlbumArtist()

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

@Composable
fun rememberPlaybackInformationState(
    activeVirtualTrack: MutableState<VirtualTrack?> = GravifonContext.activeVirtualTrack,
    playbackState: MutableState<PlaybackState> = GravifonContext.playbackState,
    playbackPositionState: MutableState<PlaybackPositionState> = GravifonContext.playbackPositionState
) = remember(activeVirtualTrack, playbackState, playbackPositionState) {
    PlaybackInformationState(
        activeVirtualTrack,
        playbackState,
        playbackPositionState
    )
}

@Composable
fun PlaybackInformationComposable(playbackControlState: PlaybackInformationState) {
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
            if (playbackControlState.playbackState.value == PlaybackState.STOPPED) {
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
                    Text(text = playbackControlState.renderArtistInformation(), fontWeight = FontWeight.ExtraBold)
                }
                Divider(color = Color.Transparent, thickness = 1.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(text = playbackControlState.renderTrackInformation(), fontWeight = FontWeight.Bold)
                    Text(text = playbackControlState.renderTrackExtraInformation(), fontWeight = FontWeight.Light)
                }
                Divider(color = Color.Transparent, thickness = 1.dp)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(text = playbackControlState.renderAlbumInformation(), fontWeight = FontWeight.Medium, fontStyle = FontStyle.Italic)
                }
            }
        }
    }
}