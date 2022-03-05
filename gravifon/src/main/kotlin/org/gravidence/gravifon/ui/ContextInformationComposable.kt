package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.qos.logback.core.util.FileSize
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.playback.PlaybackState
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.ui.image.AppIcon

class ContextInformationState(
    val playbackState: MutableState<PlaybackState>,
    val activePlaylist: MutableState<Playlist?>,
    // TODO FileSize has quite limited functionality, better to find proper alternative or implement your own
    val totalMemory: MutableState<FileSize>,
    val usedMemory: MutableState<FileSize>,
) {

    fun refresh() {
        totalMemory.value = FileSize(getTotalMemory())
        usedMemory.value = FileSize(getUsedMemory())
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
fun rememberContextInformationState(
    playbackState: MutableState<PlaybackState> = GravifonContext.playbackState,
    activePlaylist: MutableState<Playlist?> = GravifonContext.activePlaylist,
    totalMemory: MutableState<FileSize> = mutableStateOf(FileSize(ContextInformationState.getTotalMemory())),
    usedMemory: MutableState<FileSize> = mutableStateOf(FileSize(ContextInformationState.getUsedMemory())),
) = remember(playbackState, activePlaylist, totalMemory, usedMemory) {
    ContextInformationState(
        playbackState,
        activePlaylist,
        totalMemory,
        usedMemory,
    )
}

@Composable
fun ContextInformationComposable(contextInformationState: ContextInformationState) {
    val playbackSourceIconColor: Color? = if (contextInformationState.playbackState.value == PlaybackState.STOPPED) {
        Color.LightGray
    } else {
        null
    }

    Box(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Spacer(Modifier.width(3.dp))
            AppIcon(path = "icons8-audio-wave-24.png", tint = playbackSourceIconColor)
            Spacer(Modifier.width(4.dp))
            Text(text = "${contextInformationState.activePlaylist.value?.ownerName}", fontWeight = FontWeight.ExtraLight, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(10.dp))
            Text(text = "Total: ${contextInformationState.totalMemory.value}", fontWeight = FontWeight.ExtraLight)
            Spacer(Modifier.width(10.dp))
            Text(text = "Used: ${contextInformationState.usedMemory.value}", fontWeight = FontWeight.ExtraLight)
        }
    }
}