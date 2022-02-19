package org.gravidence.gravifon.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.qos.logback.core.util.FileSize
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.playlist.Playlist

class ContextInformationState(
    val activePlaylist: MutableState<Playlist?>,
    // TODO FileSize has quite limited functionality, better to find proper alternative or implement your own
    val totalMemory: MutableState<FileSize>,
    val usedMemory: MutableState<FileSize>,
) {

    fun refresh() {
        totalMemory.value = FileSize(Runtime.getRuntime().totalMemory())
        usedMemory.value = FileSize(Runtime.getRuntime().freeMemory())
    }

}

@Composable
fun rememberContextInformationState(
    activePlaylist: MutableState<Playlist?> = GravifonContext.activePlaylist,
    totalMemory: MutableState<FileSize> = mutableStateOf(FileSize(Runtime.getRuntime().totalMemory())),
    usedMemory: MutableState<FileSize> = mutableStateOf(FileSize(Runtime.getRuntime().freeMemory())),
) = remember(activePlaylist, totalMemory, usedMemory) {
    ContextInformationState(
        activePlaylist,
        totalMemory,
        usedMemory,
    )
}

@Composable
fun ContextInformationComposable(contextInformationState: ContextInformationState) {
    Box(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(text = "${contextInformationState.activePlaylist.value?.ownerName}", fontWeight = FontWeight.ExtraLight, modifier = Modifier.weight(1f))
            Spacer(Modifier.width(10.dp))
            Text(text = "Total: ${contextInformationState.totalMemory.value}", fontWeight = FontWeight.ExtraLight)
            Spacer(Modifier.width(10.dp))
            Text(text = "Used: ${contextInformationState.usedMemory.value}", fontWeight = FontWeight.ExtraLight)
        }
    }
}