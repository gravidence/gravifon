package org.gravidence.gravifon.plugin.bandcamp

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
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
import org.gravidence.gravifon.ui.util.ListHolder
import org.springframework.stereotype.Component

@Component
class BandcampView(override val playlistManager: PlaylistManager, val bandcamp: Bandcamp) : Viewable, Playable, EventAware {

    override var viewEnabled: Boolean
        get() = bandcamp.pluginEnabled
        set(value) { bandcamp.pluginEnabled = value }
    override val viewDisplayName: String
        get() = bandcamp.pluginDisplayName

    override val playlist: Playlist
    private val playlistItems: MutableState<ListHolder<PlaylistItem>>

    init {
        val cc = bandcamp.componentConfiguration.value

        playlist = playlistManager.getPlaylist(cc.playlistId)
            ?: DynamicPlaylist(
                id = cc.playlistId,
                ownerName = bandcamp.pluginDisplayName,
                displayName = cc.playlistId
            ).also { playlistManager.addPlaylist(it) }
        playlistItems = mutableStateOf(ListHolder(playlist.items()))
    }

    override fun consume(event: Event) {
        when (event) {
            is PlaylistUpdatedEvent -> {
                if (event.playlist === playlist) {
                    playlistItems.value = ListHolder(event.playlist.items())
                }
            }
        }
    }

    inner class BandcampViewState(
        val url: MutableState<String>,
        val isProcessing: MutableState<Boolean>,
    ) {

        fun addPage() {
            GravifonContext.scopeDefault.launch {
                isProcessing.value = true

                val tracks = bandcamp.parsePage(url.value)
                playlist.append(tracks.map { TrackPlaylistItem(it) })
                playlistItems.value = ListHolder(playlist.items())

                isProcessing.value = false
            }
        }

    }

    @Composable
    fun rememberBandcampViewState(
        url: String = "",
        isProcessing: Boolean = false,
    ) = remember(url, isProcessing) {
        BandcampViewState(
            url = mutableStateOf(url),
            isProcessing = mutableStateOf(isProcessing),
        )
    }

    @Composable
    override fun composeView() {
        val bandcampViewState = rememberBandcampViewState()
        val playlistState = rememberPlaylistState(
            playlistItems = playlistItems.value,
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
        IconButton(
            enabled = !bandcampViewState.isProcessing.value && bandcampViewState.url.value.isNotEmpty(),
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