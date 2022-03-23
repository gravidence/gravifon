package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.qos.logback.core.util.FileSize
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.notification.NotificationType
import org.gravidence.gravifon.playback.PlaybackStatus
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.ui.image.AppIcon
import org.gravidence.gravifon.ui.theme.smallFont
import org.gravidence.gravifon.util.DurationUtil
import kotlin.concurrent.fixedRateTimer

class ContextInformationState(
    // TODO FileSize has quite limited functionality, better to find proper alternative or implement your own
    val totalMemory: MutableState<FileSize> = mutableStateOf(FileSize(getTotalMemory())),
    val usedMemory: MutableState<FileSize> = mutableStateOf(FileSize(getUsedMemory())),
) {

    init {
        // TODO update period may go to advanced settings
        fixedRateTimer(initialDelay = 1000, period = 2000) {
            totalMemory.value = FileSize(getTotalMemory())
            usedMemory.value = FileSize(getUsedMemory())
        }
    }

    companion object {

        fun getTotalMemory(): Long {
            return Runtime.getRuntime().totalMemory()
        }

        fun getUsedMemory(): Long {
            return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
        }

    }

}

@Composable
fun ContextInformationComposable() {
    val contextInformationState = remember { ContextInformationState() }

    Box(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(Modifier.width(3.dp))
            PlaybackSourcePanel()
            Spacer(Modifier.width(10.dp))
            InnerNotificationPanel()
            Spacer(Modifier.width(10.dp))
            MemoryConsumptionPanel(contextInformationState)
        }
    }
}

@Composable
fun PlaybackSourcePanel() {
    val playbackStatus = GravifonContext.playbackStatusState.value
    val activePlaylist = GravifonContext.activePlaylist.value

    val playbackSourceIconColor = if (playbackStatus == PlaybackStatus.STOPPED) {
        Color.LightGray
    } else {
        null
    }

    val playlistOwnerName = activePlaylist?.ownerName ?: ""
    val totalDuration = activePlaylist?.run {
        items()
            .filterIsInstance<TrackPlaylistItem>()
            .mapNotNull { it.track.getLength() }
            .reduceOrNull { acc, duration -> acc.plus(duration) }
    }?.let {
        if (it.inWholeDays > 0) {
            DurationUtil.formatLongDays(it)
        } else {
            DurationUtil.formatLongHours(it)
        }
    }

    val infoBlockText = if (totalDuration == null) {
        playlistOwnerName
    } else {
        "$playlistOwnerName ($totalDuration)"
    }

    AppIcon(path = "icons8-audio-wave-24.png", tint = playbackSourceIconColor)
    Spacer(Modifier.width(4.dp))
    Text(text = infoBlockText, fontSize = smallFont, fontWeight = FontWeight.ExtraLight)
}

@Composable
fun RowScope.InnerNotificationPanel() {
    val notification = GravifonContext.activeInnerNotification.value

    val color = when (notification?.type) {
        NotificationType.REGULAR -> Color.Unspecified
        NotificationType.ERROR -> Color.Red
        else -> Color.Unspecified
    }

    Text(
        text = notification?.message ?: "",
        maxLines = 1,
        fontSize = smallFont,
        fontStyle = FontStyle.Italic,
        fontWeight = FontWeight.ExtraLight,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
    )
}

@Composable
fun MemoryConsumptionPanel(contextInformationState: ContextInformationState) {
    Text(text = "Total: ${contextInformationState.totalMemory.value}", fontSize = smallFont, fontWeight = FontWeight.ExtraLight)
    Spacer(Modifier.width(10.dp))
    Text(text = "Used: ${contextInformationState.usedMemory.value}", fontSize = smallFont, fontWeight = FontWeight.ExtraLight)
}