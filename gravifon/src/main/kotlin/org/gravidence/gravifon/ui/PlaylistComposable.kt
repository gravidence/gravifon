@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.StreamVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.domain.track.format.format
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playlist.PlayCurrentFromPlaylistEvent
import org.gravidence.gravifon.event.playlist.RemoveFromPlaylistEvent
import org.gravidence.gravifon.playback.PlaybackStatus
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.playlist.layout.ScrollPosition
import org.gravidence.gravifon.ui.component.*
import org.gravidence.gravifon.ui.image.AppIcon
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.ui.util.ListHolder
import org.gravidence.gravifon.util.DesktopUtil
import org.gravidence.gravifon.util.firstNotEmptyOrNull
import java.awt.event.MouseEvent

class PlaylistState(
    val playlistItems: MutableState<ListHolder<PlaylistItem>>,
    val playlist: Playlist
) {

    val playlistTableState = PlaylistTableState(this)

    fun selectedPlaylistItems(): List<PlaylistItem> {
        return playlistTableState.selectedRows.value.map { playlistItems.value.list[it] }
    }

    fun render(playlistItem: PlaylistItem): List<String> {
        return when (playlistItem) {
            is TrackPlaylistItem -> {
                playlist.layout.columns.map { playlistItem.track.format(it.format) }
            }
            is AlbumPlaylistItem -> TODO()
        }
    }

}

// TODO a bit too much to refresh _whole_ playlist state on active track change
@Composable
fun rememberPlaylistState(
    activeTrack: VirtualTrack? = GravifonContext.activeTrack.value,
    playlistItems: ListHolder<PlaylistItem> = ListHolder(listOf()),
    playlist: Playlist
) = remember(activeTrack, playlistItems) {
    PlaylistState(
        playlistItems = mutableStateOf(playlistItems),
        playlist = playlist
    )
}

class PlaylistTableState(
    private val playlistState: PlaylistState
) : TableState<PlaylistItem>(
    layout = layout(playlistState),
    grid = grid(playlistState),
    initialVerticalScrollPosition = playlistState.playlist.verticalScrollPosition,
) {

    override fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        return super.onKeyEvent(keyEvent) || if (keyEvent.type == KeyEventType.KeyUp) {
            when (keyEvent.key) {
                Key.Delete -> {
                    EventBus.publish(RemoveFromPlaylistEvent(playlistState.playlist, selectedRows.value))
                    selectedRows.value = setOf()
                    true
                }
                else -> false
            }
        } else {
            false
        }
    }

    override fun onRowClick(rowIndex: Int, pointerEvent: PointerEvent) {
        super.onRowClick(rowIndex, pointerEvent)
        (pointerEvent.nativeEvent as? MouseEvent)?.let {
            if (it.button == 1 && it.clickCount == 2) {
                EventBus.publish(PlayCurrentFromPlaylistEvent(playlistState.playlist, playlistState.playlistItems.value.list[rowIndex]))
            }
        }
    }

    override fun onVerticalScroll(scrollPosition: ScrollPosition) {
        super.onVerticalScroll(scrollPosition)
        playlistState.playlist.verticalScrollPosition = scrollPosition
    }

    companion object {

        fun layout(playlistState: PlaylistState): MutableState<TableLayout> {
            val columns = mutableListOf(
                TableColumn(header = "", width = 30.dp),
            )
            playlistState.playlist.layout.columns.forEach {
                columns += TableColumn(header = it.header, width = it.width.dp)
            }
            return mutableStateOf(
                TableLayout(
                    displayHeaders = true,
                    columns = columns
                )
            )
        }

        fun grid(playlistState: PlaylistState): MutableState<TableGrid<PlaylistItem>?> {
            return mutableStateOf(
                TableGrid(
                    rows = mutableStateOf(
                        playlistState.playlistItems.value.list.mapIndexed { index, playlistItem ->
                            val cells: MutableList<MutableState<TableCell<PlaylistItem>>> = mutableListOf(
                                mutableStateOf(
                                    TableCell(
                                        value = GravifonContext.playbackStatusState.value.toString(),
                                        content = { _, _, _ -> // this cell is recomposed because active track is part of rememberPlaylistState
                                            Box(
                                                modifier = Modifier
                                                    .width(30.dp)
                                                    .fillMaxHeight()
                                                    .background(color = gListItemColor, shape = gShape)
                                                    .padding(5.dp)
                                            ) {
                                                Text("") // workaround to align cell's height with other cells
                                                if (playlistState.playlist.position() == index + 1) {
                                                    AppIcon(
                                                        path = when (GravifonContext.playbackStatusState.value) {
                                                            // TODO consider icons8-musical-notes-24.png
                                                            PlaybackStatus.PLAYING -> "icons8-play-24.png"
                                                            PlaybackStatus.PAUSED -> "icons8-pause-24.png"
                                                            PlaybackStatus.STOPPED -> "icons8-stop-24.png"
                                                        },
                                                        modifier = Modifier
                                                            .align(Alignment.Center)
                                                            .size(16.dp)
                                                    )
                                                }
                                            }
                                        },
                                    )
                                )
                            )
                            playlistState.render(playlistItem).map {
                                cells += mutableStateOf(TableCell(value = it))
                            }
                            TableRow(cells)
                        }.toMutableList()
                    )
                )
            )
        }

    }

}

fun buildContextMenu(playlistState: PlaylistState): List<ContextMenuItem> {
    val contextMenuItems: MutableList<ContextMenuItem> = mutableListOf()

    val selectedItems = playlistState.selectedPlaylistItems()
    val allItems = playlistState.playlistItems.value.list

    val candidateSelectedItems: Collection<PlaylistItem>? = firstNotEmptyOrNull(
        selectedItems
    )
    if (candidateSelectedItems != null) {
        val streamTracks = candidateSelectedItems
            .filterIsInstance<TrackPlaylistItem>()
            .map { it.track }
            .filterIsInstance<StreamVirtualTrack>()
        if (streamTracks.isNotEmpty()) {
            contextMenuItems += ContextMenuItem("Open stream source page") {
                streamTracks.forEach {
                    DesktopUtil.openInBrowser(it.sourceUrl)
                }
            }
        }
    }

    val candidateItems: Collection<PlaylistItem>? = firstNotEmptyOrNull(
        candidateSelectedItems,
        allItems
    )
    if (candidateItems != null) {
        contextMenuItems += ContextMenuItem("Edit metadata") {
            GravifonContext.trackMetadataDialogState.prepare(
                playlist = playlistState.playlist,
                tracks = candidateItems
                    .filterIsInstance<TrackPlaylistItem>()
                    .map { it.track }
            )
            GravifonContext.trackMetadataDialogVisible.value = true
        }
    }

    return contextMenuItems
}

@Composable
fun PlaylistComposable(playlistState: PlaylistState) {
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .border(width = 1.dp, color = Color.Black, shape = gShape)
    ) {
        ContextMenuArea(
            items = { buildContextMenu(playlistState) }
        ) {
            Table(playlistState.playlistTableState)
        }
    }
}