@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playlist.RemovePlaylistItemsEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayCurrentEvent
import org.gravidence.gravifon.playback.PlaybackState
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.ui.image.AppIcon
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gSelectedListItemColor
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.util.DurationUtil
import org.gravidence.gravifon.util.firstNotEmptyOrNull
import org.gravidence.gravifon.util.nullableListOf
import java.awt.event.MouseEvent

class PlaylistState(
    val activeVirtualTrack: MutableState<VirtualTrack?>,
    val playlistItems: MutableState<List<PlaylistItem>>,
    val selectedPlaylistItems: MutableState<Map<Int, PlaylistItem>>,
    val preselectedPlaylistItem: MutableState<PlaylistItem?>,
    val playlist: Playlist
) {

    fun render(playlistItem: PlaylistItem): List<String> {
        return when (playlistItem) {
            is TrackPlaylistItem -> {
                val artist = playlistItem.track.getArtist()
                val title = playlistItem.track.getTitle()
                val length = DurationUtil.format(playlistItem.track.getLength())
                listOf("$artist - $title ($length)")
            }
            is AlbumPlaylistItem -> TODO()
        }
    }

    /**
     * @return true when event is handled
     */
    fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        return if (keyEvent.type == KeyEventType.KeyUp) {
            when (keyEvent.key) {
                Key.Delete -> {
                    EventBus.publish(RemovePlaylistItemsEvent(playlist, selectedPlaylistItems.value.keys))
                    selectedPlaylistItems.value = mapOf()
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }

    fun onPointerEvent(pointerEvent: PointerEvent, index: Int, playlistItem: PlaylistItem) {
        (pointerEvent.nativeEvent as? MouseEvent)?.let {
            if (it.button == 1 && it.clickCount == 2) {
                EventBus.publish(SubPlaylistPlayCurrentEvent(playlist, playlistItem))
            } else if (it.button == 1 && it.clickCount == 1 && !it.isControlDown) {
                selectedPlaylistItems.value = mapOf(Pair(index, playlistItem))
            } else if (it.button == 1 && it.clickCount == 1 && it.isControlDown) {
                if (selectedPlaylistItems.value.contains(index)) {
                    selectedPlaylistItems.value -= index
                } else {
                    selectedPlaylistItems.value += Pair(index, playlistItem)
                }
            }
        }
    }

}

@Composable
fun rememberPlaylistState(
    activeVirtualTrack: MutableState<VirtualTrack?> = GravifonContext.activeVirtualTrack,
    playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(listOf()),
    selectedPlaylistItems: MutableState<Map<Int, PlaylistItem>> = mutableStateOf(mapOf()),
    preselectedPlaylistItem: MutableState<PlaylistItem?> = mutableStateOf(null),
    playlist: Playlist
) = remember(activeVirtualTrack, playlistItems, selectedPlaylistItems, preselectedPlaylistItem) {
    PlaylistState(
        activeVirtualTrack,
        playlistItems,
        selectedPlaylistItems,
        preselectedPlaylistItem,
        playlist
    )
}

fun buildContextMenu(playlistState: PlaylistState): List<ContextMenuItem> {
    val contextMenuItems: MutableList<ContextMenuItem> = mutableListOf()

    val candidateItems: Collection<PlaylistItem>? = firstNotEmptyOrNull(
        nullableListOf(playlistState.preselectedPlaylistItem.value),
        playlistState.selectedPlaylistItems.value.values,
        playlistState.playlistItems.value
    )
    if (candidateItems != null) {
        contextMenuItems += ContextMenuItem("Edit metadata") {
            GravifonContext.trackMetadataDialogState.tracks.value = candidateItems
                .filterIsInstance<TrackPlaylistItem>()
                .map { it.track }
            GravifonContext.trackMetadataDialogState.selectedTracks.value = listOf()
            GravifonContext.trackMetadataDialogVisible.value = true
        }
    }

    return contextMenuItems
}

@Composable
fun PlaylistComposable(playlistState: PlaylistState) {
    val scrollState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .focusable()
            .focusRequester(focusRequester)
            .fillMaxHeight()
            .border(width = 1.dp, color = Color.Black, shape = gShape)
            .onPreviewKeyEvent {
                playlistState.onKeyEvent(it)
            }
            .onPointerEvent(
                eventType = PointerEventType.Press
            ) {
                focusRequester.requestFocus()
            }
            .onPointerEvent(
                eventType = PointerEventType.Scroll
            ) {
                focusRequester.requestFocus()
            }
    ) {
        ContextMenuArea(
            items = { buildContextMenu(playlistState) }
        ) {
            LazyColumn(
                state = scrollState,
                modifier = Modifier
                    .focusable()
                    .fillMaxWidth()
                    .padding(10.dp)
            ) {
                itemsIndexed(items = playlistState.playlistItems.value) { index, playlistItem ->
                    PlaylistItemComposable(index, playlistItem, playlistState)
                }
            }
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(top = 5.dp, bottom = 5.dp, end = 2.dp)
            )
        }
    }
}

val normalPlaylistItemModifier = Modifier
    .fillMaxWidth()
    .padding(5.dp)
    .background(color = gListItemColor, shape = gShape)
    .clickable { }
val selectedPlaylistItemModifier = Modifier
    .fillMaxWidth()
    .padding(5.dp)
    .background(color = gSelectedListItemColor, shape = gShape)
    .border(width = 1.dp, color = Color.Black, shape = gShape)
    .clickable { }

@Composable
fun PlaylistItemComposable(index: Int, playlistItem: PlaylistItem, playlistState: PlaylistState) {
    val fontWeight = if ((playlistItem as? TrackPlaylistItem)?.track == playlistState.activeVirtualTrack.value) {
        FontWeight.Bold
    } else {
        FontWeight.Normal
    }

    val playlistItemModifier = if (playlistState.selectedPlaylistItems.value.contains(index)) {
        selectedPlaylistItemModifier
    } else {
        normalPlaylistItemModifier
    }

    Row(
        modifier = playlistItemModifier
            .onPointerEvent(
                eventType = PointerEventType.Release
            ) {
                playlistState.onPointerEvent(it, index, playlistItem)
            }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .padding(2.dp)
                ) {
                    if (playlistState.playlist.position() == index + 1) {
                        AppIcon(
                            path = when (GravifonContext.playbackState.value) {
                                // TODO consider icons8-musical-notes-24.png
                                PlaybackState.PLAYING -> "icons8-play-24.png"
                                PlaybackState.PAUSED -> "icons8-pause-24.png"
                                PlaybackState.STOPPED -> "icons8-stop-24.png"
                            },
                            modifier = Modifier
                                .size(16.dp)
                        )
                    }
                }
                playlistState.render(playlistItem).forEach {
                    Text(
                        text = it,
                        fontWeight = fontWeight,
                        modifier = Modifier
                            .padding(5.dp)
                    )
                }
            }
        }
    }
}