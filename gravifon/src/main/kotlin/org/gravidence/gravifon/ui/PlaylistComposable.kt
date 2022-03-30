@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.StreamVirtualTrack
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
import org.gravidence.gravifon.playlist.layout.StatusColumn
import org.gravidence.gravifon.playlist.layout.TrackInfoColumn
import org.gravidence.gravifon.ui.component.*
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

}

@Composable
fun rememberPlaylistState(
    playlistItems: ListHolder<PlaylistItem> = ListHolder(listOf()),
    playlist: Playlist
) = remember(playlistItems) {
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

    override fun onRowRelease(rowIndex: Int, pointerEvent: PointerEvent) {
        super.onRowRelease(rowIndex, pointerEvent)
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

    override fun onTableColumnWidthChange(index: Int, delta: Dp) {
        super.onTableColumnWidthChange(index, delta)

        val playlistColumnsUpdated = playlistState.playlist.layout.columns.mapIndexed { i, c ->
            if (i == index) {
                val updatedWidth = layout.value.columns[i].width!! + delta
                when (c) {
                    is StatusColumn -> c.copy(width = updatedWidth.value.toInt())
                    is TrackInfoColumn -> c.copy(width = updatedWidth.value.toInt())
                }
            } else {
                c
            }
        }
        playlistState.playlist.layout = playlistState.playlist.layout.copy(columns = playlistColumnsUpdated)
    }

    companion object {

        fun layout(playlistState: PlaylistState): MutableState<TableLayout> {
            val columns = playlistState.playlist.layout.columns.map {
                TableColumn(header = it.header, width = it.width.dp)
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
                        playlistState.playlistItems.value.list.map { playlistRow(it, playlistState) }.toMutableList()
                    )
                )
            )
        }

        private fun playlistRow(playlistItem: PlaylistItem, playlistState: PlaylistState): TableRow<PlaylistItem> {
            return TableRow(
                when (playlistItem) {
                    is TrackPlaylistItem -> {
                        playlistState.playlist.layout.columns.map {
                            when (it) {
                                is StatusColumn -> mutableStateOf(statusCell(playlistItem, playlistState, it))
                                is TrackInfoColumn -> mutableStateOf(trackInfoCell(playlistItem, it))
                            }
                        }.toMutableList()
                    }
                    is AlbumPlaylistItem -> {
                        TODO("Not yet implemented")
                    }
                }
            )
        }

        private val statusIconModifier = Modifier.size(20.dp)

        private fun statusCell(trackPlaylistItem: TrackPlaylistItem, playlistState: PlaylistState, column: StatusColumn): TableCell<PlaylistItem> {
            return TableCell(
                value = GravifonContext.playbackStatusState.value.toString(),
                content = { rowIndex, _, _ ->
                    run {
                        // force cell refresh on active track change
                        GravifonContext.activeTrack.value === trackPlaylistItem.track
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color = gListItemColor, shape = gShape)
                            .padding(5.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("") // workaround to align cell's height with other cells
                            if (column.showFailureStatus && trackPlaylistItem.track.failing) {
                                TextTooltip(
                                    tooltip = "Last attempt to play the track has failed"
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Error,
                                        contentDescription = "Failure Status",
                                        modifier = statusIconModifier
                                    )
                                }
                            }
                            if (column.showPlaybackStatus && playlistState.playlist.position() == rowIndex + 1) {
                                TextTooltip(
                                    tooltip = "Playback status of playlist's current track"
                                ) {
                                    Icon(
                                        imageVector = when (GravifonContext.playbackStatusState.value) {
                                            PlaybackStatus.PLAYING -> Icons.Filled.PlayCircle
                                            PlaybackStatus.PAUSED -> Icons.Filled.PauseCircle
                                            PlaybackStatus.STOPPED -> Icons.Filled.StopCircle
                                        },
                                        contentDescription = "Playback Status",
                                        modifier = statusIconModifier
                                    )
                                }
                            }
                        }
                    }
                }
            )
        }

        private fun trackInfoCell(trackPlaylistItem: TrackPlaylistItem, column: TrackInfoColumn): TableCell<PlaylistItem> {
            return TableCell(
                value = trackPlaylistItem.track.format(column.format),
                content = { _, _, _ ->
                    Text(
                        text = trackPlaylistItem.track.format(column.format),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = gListItemColor, shape = gShape)
                            .padding(5.dp)
                    )
                }
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