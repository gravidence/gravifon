package org.gravidence.gravifon.plugin.bandcamp

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.playlist.PlaylistUpdatedEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.Playable
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.gravidence.gravifon.ui.PlaylistComposable
import org.gravidence.gravifon.ui.image.AppIcon
import org.gravidence.gravifon.ui.rememberPlaylistState
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.ui.theme.gTextFieldStyle
import org.springframework.stereotype.Component

@Component
class BandcampView(override val playlistManager: PlaylistManager, val bandcamp: Bandcamp) : Viewable, Playable, EventAware {

    override val playlist: Playlist
    private val playlistItems: MutableState<List<PlaylistItem>>

    init {
        playlist = playlistManager.getPlaylist(bandcamp.componentConfiguration.playlistId)
            ?: DynamicPlaylist(
                id = bandcamp.componentConfiguration.playlistId,
                ownerName = bandcamp.pluginDisplayName,
                displayName = bandcamp.componentConfiguration.playlistId
            ).also { playlistManager.addPlaylist(it) }
        playlistItems = mutableStateOf(playlist.items())
    }

    override fun consume(event: Event) {
        when (event) {
            is PlaylistUpdatedEvent -> {
                if (event.playlist === playlist) {
                    playlistItems.value = event.playlist.items()
                }
            }
        }
    }

    override fun viewDisplayName(): String {
        return bandcamp.pluginDisplayName
    }

    inner class BandcampViewState(
        val url: MutableState<String>,
        val isProcessing: MutableState<Boolean>,
        val playlistItems: MutableState<List<PlaylistItem>>,
        val playlist: Playlist
    ) {

        fun addPage() {
            GravifonContext.scopeDefault.launch {
                isProcessing.value = true

                val tracks = bandcamp.parsePage(url.value)
                playlist.append(tracks.map { TrackPlaylistItem(it) })
                playlistItems.value = playlist.items()

                isProcessing.value = false
            }
        }

    }

    @Composable
    fun rememberBandcampViewState(
        url: MutableState<String> = mutableStateOf(""),
        isProcessing: MutableState<Boolean> = mutableStateOf(false),
        playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(listOf()),
        playlist: Playlist
    ) = remember(url, isProcessing, playlistItems) { BandcampViewState(url, isProcessing, playlistItems, playlist) }

    @Composable
    override fun composeView() {
        val bandcampViewState = rememberBandcampViewState(
            playlistItems = playlistItems,
            playlist = playlist
        )
        val playlistState = rememberPlaylistState(
            playlistItems = playlistItems,
            playlist = playlist
        )

        Box(
            modifier = Modifier
                .padding(5.dp)
        ) {
            Column {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ControlBar(bandcampViewState)
                }
                Divider(color = Color.Transparent, thickness = 2.dp)
                Row {
                    PlaylistComposable(playlistState)
                }
            }
        }
    }

    @Composable
    fun RowScope.ControlBar(bandcampViewState: BandcampViewState) {
        BasicTextField(
            value = bandcampViewState.url.value,
            singleLine = true,
            textStyle = gTextFieldStyle,
            onValueChange = { bandcampViewState.url.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(width = 1.dp, color = Color.Black, shape = gShape)
                .padding(5.dp)
        )
        Button(
            enabled = !bandcampViewState.isProcessing.value && bandcampViewState.url.value.isNotEmpty(),
            contentPadding = PaddingValues(0.dp),
            onClick = { bandcampViewState.addPage() }
        ) {
            if (bandcampViewState.isProcessing.value) {
                CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
            } else {
                AppIcon("icons8-plus-+-24.png")
            }
        }
    }

}