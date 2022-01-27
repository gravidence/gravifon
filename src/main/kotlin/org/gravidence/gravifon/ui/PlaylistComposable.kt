package org.gravidence.gravifon.ui

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayCurrentEvent
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import java.awt.event.MouseEvent

class PlaylistState(val playlistItems: MutableState<List<PlaylistItem>>, val playlist: Playlist) {

    fun render(playlistItem: PlaylistItem): List<String> {
        return when (playlistItem) {
            is TrackPlaylistItem -> listOf("${playlistItem.track.getArtist()} - ${playlistItem.track.getTitle()}")
            is AlbumPlaylistItem -> TODO()
        }
    }

    fun onPointerEvent(pointerEvent: PointerEvent, playlistItem: PlaylistItem) {
        (pointerEvent.nativeEvent as? MouseEvent)?.apply {
            if (button == 1 && clickCount == 2) {
                playlist.moveToSpecific(playlistItem)
                EventBus.publish(SubPlaylistPlayCurrentEvent())
            }
        }
    }

}

@Composable
fun rememberPlaylistState(
    playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(listOf()),
    playlist: Playlist
) = remember(playlistItems, playlist) { PlaylistState(playlistItems, playlist) }

@Composable
fun PlaylistComposable(playlistState: PlaylistState) {
    val scrollState = rememberLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
    ) {
        LazyColumn(
            state = scrollState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            items(playlistState.playlistItems.value) {
                PlaylistItemComposable(it, playlistState)
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(scrollState),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(top = 5.dp, bottom = 5.dp, end = 2.dp)
                .fillMaxHeight()
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PlaylistItemComposable(playlistItem: PlaylistItem, playlistState: PlaylistState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .background(color = Color.LightGray, shape = RoundedCornerShape(5.dp))
            .onPointerEvent(
                eventType = PointerEventType.Release,
                onEvent = {
                    playlistState.onPointerEvent(it, playlistItem)
                }
            )
    ) {
        playlistState.render(playlistItem).forEach {
            Text(
                text = it,
                modifier = Modifier.padding(5.dp)
            )
        }
    }
}