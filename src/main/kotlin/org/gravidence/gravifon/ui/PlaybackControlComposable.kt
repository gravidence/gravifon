package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.Gravifon
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playback.SubPlaybackPauseEvent
import org.gravidence.gravifon.event.playback.SubPlaybackPositionEvent
import org.gravidence.gravifon.event.playback.SubPlaybackStopEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayCurrentEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayNextEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayPrevEvent
import org.gravidence.gravifon.playback.PlaybackState
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.util.DurationUtil
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PlaybackControlState(
    val activeVirtualTrack: MutableState<VirtualTrack?>,
    val playbackState: MutableState<PlaybackState>,
    val playbackPositionState: PlaybackPositionState,
    val activePlaylist: MutableState<Playlist?>
) {

    fun onPrev() {
        activePlaylist.value?.let { EventBus.publish(SubPlaylistPlayPrevEvent(it)) }
    }

    fun onStop() {
        EventBus.publish(SubPlaybackStopEvent())
    }

    fun onPause() {
        EventBus.publish(SubPlaybackPauseEvent())
    }

    fun onPlay() {
        activePlaylist.value?.let { EventBus.publish(SubPlaylistPlayCurrentEvent(it)) }
    }

    fun onNext() {
        activePlaylist.value?.let { EventBus.publish(SubPlaylistPlayNextEvent(it)) }
    }

    fun onPositionChange(rawPosition: Float) {
        val position = rawPosition.toLong().toDuration(DurationUnit.MILLISECONDS)
        EventBus.publish(SubPlaybackPositionEvent(position))
    }

    fun elapsedTime(): String {
        return DurationUtil.format(playbackPositionState.runningPosition.value)
    }

    fun remainingTime(): String {
        return DurationUtil.format(playbackPositionState.endingPosition.value.minus(playbackPositionState.runningPosition.value))
    }

}

@Composable
fun rememberPlaybackControlState(
    activeVirtualTrack: MutableState<VirtualTrack?> = Gravifon.activeVirtualTrack,
    playbackState: MutableState<PlaybackState> = Gravifon.playbackState,
    playbackPositionState: PlaybackPositionState = Gravifon.playbackPositionState,
    activePlaylist: MutableState<Playlist?> = Gravifon.activePlaylist
) = remember(activeVirtualTrack, playbackState, playbackPositionState, activePlaylist) {
    PlaybackControlState(
        activeVirtualTrack,
        playbackState,
        playbackPositionState,
        activePlaylist
    )
}

@Composable
fun PlaybackControlComposable(playbackControlState: PlaybackControlState) {
    Box(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Button(
                    onClick = playbackControlState::onPrev,
                ) {
                    Text("Prev")
                }
                Button(
                    onClick = playbackControlState::onStop,
//                    modifier = if (playbackControlState.playbackState.value == PlaybackState.STOPPED) {
//                        Modifier
//                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
//                    } else {
//                        null
//                    }
                ) {
                    Text("Stop")
                }
                Button(
                    onClick = playbackControlState::onPause,
                ) {
                    Text("Pause")
                }
                Button(
                    onClick = playbackControlState::onPlay,
                ) {
                    Text("Play")
                }
                Button(
                    onClick = playbackControlState::onNext,
                ) {
                    Text("Next")
                }
                Text(text = playbackControlState.elapsedTime(), fontWeight = FontWeight.Light)
                Slider(
                    value = playbackControlState.playbackPositionState.runningPosition.value
                        .inWholeMilliseconds.toFloat(),
                    valueRange = 0f..playbackControlState.playbackPositionState.endingPosition.value
                        .inWholeMilliseconds.toFloat(),
                    onValueChange = {
                        playbackControlState.onPositionChange(it)
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f) // TODO workaround for https://github.com/JetBrains/compose-jb/issues/1765
                )
                Text(text = "-${playbackControlState.remainingTime()}", fontWeight = FontWeight.Light)
            }
        }
    }
}