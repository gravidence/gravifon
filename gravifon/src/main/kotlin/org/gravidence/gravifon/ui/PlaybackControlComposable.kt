package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playback.PausePlaybackEvent
import org.gravidence.gravifon.event.playback.RepositionPlaybackPointAbsoluteEvent
import org.gravidence.gravifon.event.playback.StopPlaybackEvent
import org.gravidence.gravifon.event.playlist.PlayCurrentFromPlaylistEvent
import org.gravidence.gravifon.event.playlist.PlayNextFromPlaylistEvent
import org.gravidence.gravifon.event.playlist.PlayPreviousFromPlaylistEvent
import org.gravidence.gravifon.playback.PlaybackStatus
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.util.DurationUtil
import org.gravidence.gravifon.util.DualStateObject
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
    val playbackButtonModifier = remember {
        DualStateObject(
            Modifier
                .size(48.dp)
                .padding(0.dp),
            Modifier
                .size(52.dp)
                .padding(0.dp)
        )
    }

    val playbackIconModifier = remember {
        DualStateObject(
            Modifier
                .requiredSize(24.dp),
            Modifier
                .requiredSize(32.dp)
        )
    }

    val playbackStatus = GravifonContext.playbackStatusState.value

    Button(
        onClick = PlaybackControlState::onPrev,
        modifier = playbackButtonModifier.state()
    ) {
        Icon(
            imageVector = Icons.Filled.SkipPrevious,
            contentDescription = "Previous",
            modifier = playbackIconModifier.state()
        )
    }
    Button(
        onClick = PlaybackControlState::onStop,
        modifier = playbackButtonModifier.state(playbackStatus != PlaybackStatus.STOPPED)
    ) {
        Icon(
            imageVector = Icons.Filled.Stop,
            contentDescription = "Stop",
            modifier = playbackIconModifier.state(playbackStatus != PlaybackStatus.STOPPED)
        )
    }
    Button(
        onClick = PlaybackControlState::onPause,
        modifier = playbackButtonModifier.state(playbackStatus != PlaybackStatus.PAUSED)
    ) {
        Icon(
            imageVector = Icons.Filled.Pause,
            contentDescription = "Pause",
            modifier = playbackIconModifier.state(playbackStatus != PlaybackStatus.PAUSED)
        )
    }
    Button(
        onClick = PlaybackControlState::onPlay,
        modifier = playbackButtonModifier.state(playbackStatus != PlaybackStatus.PLAYING)
    ) {
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            modifier = playbackIconModifier.state(playbackStatus != PlaybackStatus.PLAYING)
        )
    }
    Button(
        onClick = PlaybackControlState::onNext,
        modifier = playbackButtonModifier.state()
    ) {
        Icon(
            imageVector = Icons.Filled.SkipNext,
            contentDescription = "Next",
            modifier = playbackIconModifier.state()
        )
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