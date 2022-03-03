package org.gravidence.gravifon.plugin.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.domain.track.virtualTrackComparator
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
import org.gravidence.gravifon.query.TrackQueryParser
import org.gravidence.gravifon.ui.PlaylistComposable
import org.gravidence.gravifon.ui.rememberPlaylistState
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.ui.theme.gTextFieldColor
import org.gravidence.gravifon.ui.theme.gTextFieldStyle
import org.gravidence.gravifon.ui.util.ListHolder
import org.springframework.stereotype.Component

@Component
class LibraryView(override val playlistManager: PlaylistManager, val library: Library) : Viewable, Playable, EventAware {

    override val playlist: Playlist
    private val playlistItems: MutableState<ListHolder<PlaylistItem>>

    init {
        val cc = library.componentConfiguration.value

        playlist = playlistManager.getPlaylist(cc.playlistId)
            ?: DynamicPlaylist(
                id = cc.playlistId,
                ownerName = library.pluginDisplayName,
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

    override fun viewDisplayName(): String {
        return library.pluginDisplayName
    }

    inner class LibraryViewState(
        val query: MutableState<String>,
    ) {

        fun onQueryChange(changed: String) {
            query.value = changed
            if (TrackQueryParser.validate(changed)) {
                val cc = library.componentConfiguration.value

                val selection = TrackQueryParser.execute(changed, library.libraryStorage.allTracks())
                    // TODO sortOrder should be part of view state
                    .sortedWith(virtualTrackComparator(cc.sortOrder))
                    .map { TrackPlaylistItem(it) }
                playlist.replace(selection)
                playlistItems.value = ListHolder(playlist.items())

                if (cc.queryHistory.size >= cc.queryHistorySizeLimit) {
                    cc.queryHistory.removeLast()
                }
                cc.queryHistory.add(0, changed)
            }
        }

    }

    @Composable
    fun rememberLibraryViewState(
        query: String = "",
    ) = remember(query) {
        LibraryViewState(
            query = mutableStateOf(query)
        )
    }

    @Composable
    override fun composeView() {
        val libraryViewState = rememberLibraryViewState(
            query = library.componentConfiguration.value.queryHistory.firstOrNull() ?: "",
        )
        val playlistState = rememberPlaylistState(
            playlistItems = playlistItems.value,
            playlist = playlist
        )

        Box(
            modifier = Modifier
                .padding(5.dp)
        ) {
            Column {
                Row {
                    QueryBar(libraryViewState)
                }
                Divider(color = Color.Transparent, thickness = 2.dp)
                Row {
                    PlaylistComposable(playlistState)
                }
            }
        }
    }

    @Composable
    fun QueryBar(libraryViewState: LibraryViewState) {
        Box(
            modifier = Modifier
                .height(50.dp)
                .border(width = 1.dp, color = Color.Black, shape = gShape)
                .background(color = gTextFieldColor, shape = gShape)
        ) {
            BasicTextField(
                value = libraryViewState.query.value,
                singleLine = true,
                textStyle = gTextFieldStyle,
                onValueChange = { libraryViewState.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .align(Alignment.CenterStart)
            )
        }
    }

}