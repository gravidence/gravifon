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
import org.gravidence.gravifon.ui.theme.*
import org.springframework.stereotype.Component

@Component
class LibraryView(override val playlistManager: PlaylistManager, val library: Library) : Viewable, Playable, EventAware {

    override val playlist: Playlist
    private val playlistItems: MutableState<List<PlaylistItem>>

    init {
        playlist = playlistManager.getPlaylist(library.componentConfiguration.playlistId)
            ?: DynamicPlaylist(
                id = library.componentConfiguration.playlistId,
                ownerName = library.pluginDisplayName,
                displayName = library.componentConfiguration.playlistId
            ).also { playlistManager.addPlaylist(it) }
        playlistItems = mutableStateOf(playlist.items())
    }

    override fun consume(event: Event) {
        if (event is PlaylistUpdatedEvent) {
            playlistItems.value = event.playlist.items()
        }
    }

    override fun viewDisplayName(): String {
        return library.pluginDisplayName
    }

    inner class LibraryViewState(
        val query: MutableState<String>,
        val playlistItems: MutableState<List<PlaylistItem>>,
        val playlist: Playlist
    ) {

        fun onQueryChange(changed: String) {
            query.value = changed
            if (TrackQueryParser.validate(changed)) {
                val selection = TrackQueryParser.execute(changed, library.libraryStorage.allTracks())
                    // TODO sortOrder should be part of view state
                    .sortedWith(virtualTrackComparator(library.componentConfiguration.sortOrder))
                    .map { TrackPlaylistItem(it) }
                playlist.replace(selection)
                playlistItems.value = playlist.items()

                if (library.componentConfiguration.queryHistory.size >= library.componentConfiguration.queryHistorySizeLimit) {
                    library.componentConfiguration.queryHistory.removeLast()
                }
                library.componentConfiguration.queryHistory.add(0, changed)
            }
        }

    }

    @Composable
    fun rememberLibraryViewState(
        query: MutableState<String> = mutableStateOf(""),
        playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(listOf()),
        playlist: Playlist
    ) = remember(query, playlistItems) { LibraryViewState(query, playlistItems, playlist) }

    @Composable
    override fun composeView() {
        val libraryViewState = rememberLibraryViewState(
            query = mutableStateOf(library.componentConfiguration.queryHistory.firstOrNull() ?: ""),
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