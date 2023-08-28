package org.gravidence.gravifon.ui.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.tag.FieldValue
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playlist.PlaylistUpdatedEvent
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.ui.component.Table
import org.gravidence.gravifon.ui.component.TableCell
import org.gravidence.gravifon.ui.component.TableColumn
import org.gravidence.gravifon.ui.component.TableGrid
import org.gravidence.gravifon.ui.component.TableLayout
import org.gravidence.gravifon.ui.component.TableRow
import org.gravidence.gravifon.ui.component.TableState
import org.gravidence.gravifon.ui.component.singleColumnTableGrid
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.ui.util.ListHolder

class TrackMetadataState(
    private var playlist: Playlist? = null,
    val tracks: MutableState<List<VirtualTrack>>,
    val tracksSnapshot: MutableState<ListHolder<VirtualTrack>> = mutableStateOf(snapshot(tracks.value)),
    val selectedTracks: MutableState<Set<Int>>, // refers to tracks from snapshot, aka selected for edit, but results could be reverted
    val changed: MutableState<Boolean> = mutableStateOf(false)
) {

    fun prepare(playlist: Playlist, tracks: List<VirtualTrack>) {
        this.playlist = playlist
        this.tracks.value = tracks
        this.tracksSnapshot.value = snapshot(tracks)
        this.selectedTracks.value = List(tracks.size) { index -> index }.toSet() // user is likely expects all selected by default
        this.changed.value = false
    }

    fun apply() {
        tracksSnapshot.value.list.forEach { snapshotTrack ->
            tracks.value.find { originalTrack -> originalTrack.uri() == snapshotTrack.uri() }?.apply {
                fields.apply {
                    clear()
                    putAll(snapshotTrack.fields)
                }
                customFields.apply {
                    clear()
                    putAll(snapshotTrack.customFields)
                }
            }
        }
        changed.value = false

        playlist?.let { EventBus.publish(PlaylistUpdatedEvent(it)) }
    }

    fun revert() {
        tracksSnapshot.value = snapshot(tracks.value)
        changed.value = false
    }

    fun clear() {
        tracks.value = listOf()
        tracksSnapshot.value = ListHolder(listOf())
        selectedTracks.value = setOf()
        changed.value = false
    }

    companion object {

        fun snapshot(tracks: List<VirtualTrack>): ListHolder<VirtualTrack> {
            return ListHolder(tracks.map { it.clone() })
        }

    }

}

class TrackMetadataListState(
    trackMetadataState: TrackMetadataState
) : TableState<VirtualTrack>(
    layout = layout(),
    grid = grid(trackMetadataState),
    selectedRows = trackMetadataState.selectedTracks // share dialog's selection set with the table
) {

    companion object {

        fun layout(): MutableState<TableLayout> {
            return mutableStateOf(
                TableLayout(
                    displayHeaders = true,
                    columns = listOf(
                        TableColumn(header = "Track", fraction = 1f)
                    )
                )
            )
        }

        fun grid(trackMetadataState: TrackMetadataState): MutableState<TableGrid<VirtualTrack>?> {
            return mutableStateOf(singleColumnTableGrid(
                trackMetadataState.tracks.value.map {
                    TableCell(value = it.uri().toString())
                }
            ))
        }

    }

}

class TrackMetadataTableState(
    private val trackMetadataState: TrackMetadataState
) : TableState<List<VirtualTrack>>(
    layout = layout(),
    enabled = mutableStateOf(true),
    grid = grid(trackMetadataState)
) {

    override fun onCellChange(cell: TableCell<List<VirtualTrack>>, rowIndex: Int, columnIndex: Int, newValue: String) {
        super.onCellChange(cell, rowIndex, columnIndex, newValue)
        grid.value?.let { table ->
            cell.source?.forEach { track ->
                track.setFieldValues(table.rows.value[rowIndex].cells[0].value.value!!, newValue)
            }
        }
        trackMetadataState.changed.value = true
    }

    companion object {

        fun layout(): MutableState<TableLayout> {
            return mutableStateOf(
                TableLayout(
                    displayHeaders = true,
                    columns = listOf(
                        TableColumn(header = "Tag", fraction = 0.25f),
                        TableColumn(header = "Value", fraction = 1f)
                    )
                )
            )
        }

        fun grid(trackMetadataState: TrackMetadataState): MutableState<TableGrid<List<VirtualTrack>>?> {
            return mutableStateOf(buildTableGrid(
                trackMetadataState.selectedTracks.value.map { trackMetadataState.tracksSnapshot.value.list[it] }
            ))
        }

    }

}

private fun closeDialog() {
    GravifonContext.trackMetadataDialogState.clear()
    GravifonContext.trackMetadataDialogVisible.value = false
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TrackMetadataDialog() {
    if (GravifonContext.trackMetadataDialogVisible.value) {
        DialogWindow(
            title = "Edit Metadata",
            visible = GravifonContext.trackMetadataDialogVisible.value,
            state = rememberDialogState(
                position = WindowPosition(Alignment.Center),
                size = DpSize(800.dp, 600.dp)
            ),
            onPreviewKeyEvent = {
                if (it.key == Key.Escape && !GravifonContext.trackMetadataDialogState.changed.value) {
                    closeDialog()
                    true
                } else {
                    false
                }
            },
            onCloseRequest = { closeDialog() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .padding(5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(0.4f)
                            .border(width = 1.dp, color = Color.Black, shape = gShape)
                    ) {
                        TrackList(GravifonContext.trackMetadataDialogState)
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .border(width = 1.dp, color = Color.Black, shape = gShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            TrackContent(GravifonContext.trackMetadataDialogState)
                        }
                        Divider(
                            thickness = 2.dp,
                            modifier = Modifier
                                .padding(horizontal = 10.dp)
                        )
                        Box {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.End),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 15.dp, vertical = 10.dp)
                            ) {
                                DialogControls(GravifonContext.trackMetadataDialogState)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrackList(trackMetadataState: TrackMetadataState) {
    Table(TrackMetadataListState(trackMetadataState))
}

@Composable
fun TrackContent(trackMetadataState: TrackMetadataState) {
    Table(TrackMetadataTableState(trackMetadataState))
}

@Composable
fun DialogControls(trackMetadataState: TrackMetadataState) {
    Button(
        enabled = trackMetadataState.changed.value,
        onClick = {
            trackMetadataState.apply()
            closeDialog()
        }
    ) {
        Text("Apply & Close")
    }
    Button(
        enabled = trackMetadataState.changed.value,
        onClick = { trackMetadataState.apply() }
    ) {
        Text("Apply")
    }
    Button(
        enabled = trackMetadataState.changed.value,
        onClick = { trackMetadataState.revert() }
    ) {
        Text("Revert")
    }
}

private fun buildTableGrid(tracks: List<VirtualTrack>): TableGrid<List<VirtualTrack>>? {
    return when (tracks.size) {
        0 -> null
        1 -> tracks.first().buildTableGridForSingleTrack()
        else -> tracks.buildTableGridForManyTracks()
    }
}

private fun VirtualTrack.buildTableGridForSingleTrack(): TableGrid<List<VirtualTrack>> {
    val fieldRows = fields.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            TableRow(
                cells = mutableListOf(
                    mutableStateOf(TableCell(value = fieldKey.name, source = listOf(this))),
                    mutableStateOf(TableCell(value = fieldValue, source = listOf(this)))
                )
            )
        }
    }
    val customFieldRows = customFields.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            TableRow(
                cells = mutableListOf(
                    mutableStateOf(TableCell(value = fieldKey, source = listOf(this))),
                    mutableStateOf(TableCell(value = fieldValue, source = listOf(this)))
                )
            )
        }
    }

    return TableGrid(
        rows = mutableStateOf((fieldRows + customFieldRows).toMutableList())
    )
}

private fun List<VirtualTrack>.buildTableGridForManyTracks(): TableGrid<List<VirtualTrack>> {
    return TableGrid(
        rows = mutableStateOf(
            toTagMap().map {
                TableRow(
                    cells = mutableListOf(
                        mutableStateOf(TableCell(value = it.key, source = this)),
                        mutableStateOf(TableCell(value = it.value, source = this))
                    )
                )
            }.toMutableList()
        )
    )
}

private fun List<VirtualTrack>.toTagMap(): Map<String, FieldValue?> {
    val tagMap = mutableMapOf<String, FieldValue?>()

    this.forEach { track ->
        track.getAllFields().forEach { (fieldKey, fieldValues) ->
            fieldValues.values.forEach { fieldValue ->
                if (tagMap.containsKey(fieldKey)) {
                    val prevFieldValue = tagMap[fieldKey]
                    if (prevFieldValue != null && prevFieldValue != fieldValue) {
                        tagMap[fieldKey] = null
                    }
                } else {
                    tagMap[fieldKey] = fieldValue
                }
            }
        }
    }

    return tagMap
}