package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.Gravifon
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playback.*
import org.gravidence.gravifon.event.playlist.SubPlaylistActivatePriorityPlaylistEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistActivateRegularPlaylistEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayNextEvent
import org.gravidence.gravifon.playback.PlaybackState

class PlaybackControlState(val activeVirtualTrack: MutableState<VirtualTrack?>, val playbackState: MutableState<PlaybackState>, val playbackSliderState: SliderState) {

    init {
        EventBus.subscribe {
            when (it) {
                is PubPlaybackStartEvent -> {
                    playbackState.value = PlaybackState.PLAYING
                    playbackSliderState.steps.value = it.length.toInt()
                    playbackSliderState.positionRange.value = 0f..it.length.toFloat()
                }
                is SubPlaybackPauseEvent -> {
                    playbackState.value = PlaybackState.PAUSED
                }
                is SubPlaybackStopEvent -> {
                    playbackState.value = PlaybackState.STOPPED
                    playbackSliderState.steps.value = 0
                    playbackSliderState.positionRange.value = 0f..0f
                }
                is PubPlaybackPositionEvent -> playbackSliderState.position.value = it.position.toFloat()
            }
        }
    }

    fun onPrev() {

    }

    fun onStop() {
        EventBus.publish(SubPlaybackStopEvent())
    }

    fun onPause() {
        EventBus.publish(SubPlaybackPauseEvent())
    }

    fun onPlay() {
        EventBus.publish(SubPlaylistActivatePriorityPlaylistEvent(null))
        EventBus.publish(SubPlaylistActivateRegularPlaylistEvent(null))
        EventBus.publish(SubPlaylistPlayNextEvent())
    }

    fun onNext() {

    }

    fun onPositionChange(position: Float) {
        playbackSliderState.position.value = position
        EventBus.publish(SubPlaybackPositionEvent(position.toLong()))
    }

}

@Composable
fun rememberPlaybackControlState(
    activeVirtualTrack: MutableState<VirtualTrack?> = Gravifon.activeVirtualTrack,
    playbackState: MutableState<PlaybackState> = Gravifon.playbackState,
    playbackSliderState: SliderState = SliderState(mutableStateOf(0f), mutableStateOf(0), mutableStateOf(0f..0f))
) = remember(activeVirtualTrack, playbackState, playbackSliderState) { PlaybackControlState(activeVirtualTrack, playbackState, playbackSliderState) }

@Composable
fun PlaybackControlComposable(playbackControlState: PlaybackControlState) {
    Box(
        modifier = Modifier
            .padding(5.dp)
//            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
    ) {
            Row(
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
                Slider(
                    value = playbackControlState.playbackSliderState.position.value,
                    steps = playbackControlState.playbackSliderState.steps.value,
                    valueRange = playbackControlState.playbackSliderState.positionRange.value,
                    onValueChange = { playbackControlState.onPositionChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
//                        .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                )
            }
    }
}