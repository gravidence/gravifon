package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playback.RepositionPlaybackPointAbsoluteEvent
import org.gravidence.gravifon.event.playback.PausePlaybackEvent
import org.gravidence.gravifon.event.playback.StopPlaybackEvent
import org.gravidence.gravifon.event.playlist.PlayCurrentFromPlaylistEvent
import org.gravidence.gravifon.event.playlist.PlayNextFromPlaylistEvent
import org.gravidence.gravifon.event.playlist.PlayPreviousFromPlaylistEvent
import org.gravidence.gravifon.playback.PlaybackStatus
import org.gravidence.gravifon.ui.image.AppIcon
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.util.DurationUtil
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class PlaybackControlState {

    companion object {

        fun onPrev() {
            GravifonContext.activePlaylist.value?.let { EventBus.publish(PlayPreviousFromPlaylistEvent(it)) }
        }

        fun onStop() {
            EventBus.publish(StopPlaybackEvent())
        }

        fun onPause() {
            EventBus.publish(PausePlaybackEvent())
        }

        fun onPlay() {
            GravifonContext.activePlaylist.value?.let { EventBus.publish(PlayCurrentFromPlaylistEvent(it)) }
        }

        fun onNext() {
            GravifonContext.activePlaylist.value?.let { EventBus.publish(PlayNextFromPlaylistEvent(it)) }
        }

        fun onPositionChange(rawPosition: Float) {
            val position = rawPosition.toLong().toDuration(DurationUnit.MILLISECONDS)
            EventBus.publish(RepositionPlaybackPointAbsoluteEvent(position))
        }

        fun elapsedTime(playbackPositionState: PlaybackPositionState): String {
            return DurationUtil.formatShortHours(playbackPositionState.runningPosition)
        }

        fun remainingTime(playbackPositionState: PlaybackPositionState): String {
            return DurationUtil.formatShortHours(playbackPositionState.endingPosition.minus(playbackPositionState.runningPosition))
        }

    }

}

private val defaultPlaybackButtonModifier = Modifier
    .size(48.dp)
    .padding(0.dp)
private val activePlaybackButtonModifier = Modifier
    .size(52.dp)
    .padding(0.dp)

@Composable
fun PlaybackControlComposable() {
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
                PlaybackControl()
                Spacer(Modifier.width(5.dp))
                PositionControl()
            }
        }
    }
}

@Composable
fun PlaybackControl() {
    val playbackStatus = GravifonContext.playbackStatusState.value

    Button(
        onClick = PlaybackControlState::onPrev,
        modifier = defaultPlaybackButtonModifier
    ) {
        AppIcon("icons8-skip-to-start-24.png")
    }
    Button(
        onClick = PlaybackControlState::onStop,
        modifier = if (playbackStatus == PlaybackStatus.STOPPED) {
            activePlaybackButtonModifier
        } else {
            defaultPlaybackButtonModifier
        }
    ) {
        AppIcon("icons8-stop-24.png")
    }
    Button(
        onClick = PlaybackControlState::onPause,
        modifier = if (playbackStatus == PlaybackStatus.PAUSED) {
            activePlaybackButtonModifier
        } else {
            defaultPlaybackButtonModifier
        }
    ) {
        AppIcon("icons8-pause-24.png")
    }
    Button(
        onClick = PlaybackControlState::onPlay,
        modifier = if (playbackStatus == PlaybackStatus.PLAYING) {
            activePlaybackButtonModifier
        } else {
            defaultPlaybackButtonModifier
        }
    ) {
        AppIcon("icons8-play-24.png")
    }
    Button(
        onClick = PlaybackControlState::onNext,
        modifier = defaultPlaybackButtonModifier
    ) {
        AppIcon("icons8-end-24.png")
    }
}

@Composable
fun RowScope.PositionControl() {
    val playbackPositionState = GravifonContext.playbackPositionState.value

    Text(text = PlaybackControlState.elapsedTime(playbackPositionState), fontWeight = FontWeight.Light)
    Slider(
        value = playbackPositionState.runningPosition
            .inWholeMilliseconds.toFloat(),
        valueRange = 0f..playbackPositionState.endingPosition
            .inWholeMilliseconds.toFloat(),
        onValueChange = {
            PlaybackControlState.onPositionChange(it)
        },
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
    )
    Text(text = "-${PlaybackControlState.remainingTime(playbackPositionState)}", fontWeight = FontWeight.Light)
}