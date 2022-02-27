package org.gravidence.gravifon.ui.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.tag.FieldValue
import org.gravidence.gravifon.domain.tag.FieldValues
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.ui.component.*
import org.gravidence.gravifon.ui.theme.gShape

class TrackMetadataState(
    val tracks: MutableState<List<VirtualTrack>>,
    val tracksSnapshot: MutableState<List<VirtualTrack>> = mutableStateOf(snapshot(tracks.value)),
    val selectedTracks: MutableState<Set<Int>>, // refers to tracks from snapshot, aka selected for edit, but results could be reverted
    val changed: MutableState<Boolean> = mutableStateOf(false)
) {

    fun prepare(tracks: List<VirtualTrack>) {
        this.tracks.value = tracks
        this.tracksSnapshot.value = snapshot(tracks)
        this.selectedTracks.value = mutableSetOf()
        this.changed.value = false
    }

    fun revert() {
        tracksSnapshot.value = snapshot(tracks.value)
        changed.value = false
    }

    fun clear() {
        tracks.value = mutableListOf()
        tracksSnapshot.value = mutableListOf()
        selectedTracks.value = mutableSetOf()
        changed.value = false
    }

    companion object {

        fun snapshot(tracks: List<VirtualTrack>): List<VirtualTrack> {
            return tracks.map { it.clone() }
        }

    }

}

class TrackMetadataListState(
    private val trackMetadataState: TrackMetadataState
) : TableState<VirtualTrack>(
    layout = layout(),
    readOnly = mutableStateOf(true),
    grid = grid(trackMetadataState)
) {

    override fun onRowClick(rowIndex: Int, pointerEvent: PointerEvent) {
        super.onRowClick(rowIndex, pointerEvent)
        trackMetadataState.selectedTracks.value = selectedRows.value
    }

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
                    TableCell(content = it.uri().toString())
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
    readOnly = mutableStateOf(false),
    grid = grid(trackMetadataState)
) {

    override fun onCellChange(cell: TableCell<List<VirtualTrack>>, rowIndex: Int, columnIndex: Int, newValue: String) {
        super.onCellChange(cell, rowIndex, columnIndex, newValue)
        grid.value?.let { table ->
            val tagMap = table.toTagMap()
            cell.source?.forEach { track ->
                track.fields.clear()
                track.customFields.clear()
                tagMap.entries.forEach {
                    track.setFieldValues(it.key, it.value)
                }
            }
        }
        trackMetadataState.tracksSnapshot.value = trackMetadataState.tracksSnapshot.value.toList()
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
                trackMetadataState.selectedTracks.value.map { trackMetadataState.tracksSnapshot.value[it] }
            ))
        }

    }

}

@Composable
fun TrackMetadataDialog() {
    if (GravifonContext.trackMetadataDialogVisible.value) {
        Dialog(
            title = "Edit Metadata",
            visible = GravifonContext.trackMetadataDialogVisible.value,
            state = rememberDialogState(
                position = WindowPosition(Alignment.Center),
                size = DpSize(800.dp, 600.dp)
            ),
            onCloseRequest = {
                GravifonContext.trackMetadataDialogState.clear()
                GravifonContext.trackMetadataDialogVisible.value = false
            }
        ) {
            val pluginListVScrollState = rememberScrollState()
            val pluginSettingsVScrollState = rememberScrollState()

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
                        Box {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(10.dp)
                                    .verticalScroll(state = pluginListVScrollState)
                            ) {
                                trackListContent(GravifonContext.trackMetadataDialogState)
                            }
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(pluginListVScrollState),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(top = 5.dp, bottom = 5.dp, end = 2.dp)
                            )
                        }
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
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp)
                                    .verticalScroll(state = pluginSettingsVScrollState)
                            ) {
                                trackContent(GravifonContext.trackMetadataDialogState)
                            }
                            VerticalScrollbar(
                                adapter = rememberScrollbarAdapter(pluginSettingsVScrollState),
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(top = 5.dp, bottom = 5.dp, end = 2.dp)
                            )
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
                                Button(
                                    enabled = GravifonContext.trackMetadataDialogState.changed.value,
                                    onClick = {}
                                ) {
                                    Text("Apply")
                                }
                                Button(
                                    enabled = GravifonContext.trackMetadataDialogState.changed.value,
                                    onClick = { GravifonContext.trackMetadataDialogState.revert() }
                                ) {
                                    Text("Revert")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun trackListContent(trackMetadataState: TrackMetadataState) {
    Table(TrackMetadataListState(trackMetadataState))
}

@Composable
fun trackContent(trackMetadataState: TrackMetadataState) {
    Table(TrackMetadataTableState(trackMetadataState))
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
                    mutableStateOf(TableCell(content = fieldKey.name, source = listOf(this))),
                    mutableStateOf(TableCell(content = fieldValue, source = listOf(this)))
                )
            )
        }
    }
    val customFieldRows = customFields.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            TableRow(
                cells = mutableListOf(
                    mutableStateOf(TableCell(content = fieldKey, source = listOf(this))),
                    mutableStateOf(TableCell(content = fieldValue, source = listOf(this)))
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
                        mutableStateOf(TableCell(content = it.key, source = this)),
                        mutableStateOf(TableCell(content = it.value, source = this))
                    )
                )
            }.toMutableList()
        )
    )
}

private fun List<VirtualTrack>.toTagMap(): Map<String, FieldValue?> {
    val tagMap = mutableMapOf<String, FieldValue?>()

    this.forEach { track ->
        track.fields.forEach { (fieldKey, fieldValues) ->
            val key = fieldKey.name
            fieldValues.values.forEach { fieldValue ->
                if (tagMap.containsKey(key)) {
                    val prevFieldValue = tagMap[key]
                    if (prevFieldValue != null && prevFieldValue != fieldValue) {
                        tagMap[key] = null
                    }
                } else {
                    tagMap[key] = fieldValue
                }
            }
        }
    }

    return tagMap
}

private fun TableGrid<List<VirtualTrack>>.toTagMap(): Map<String, FieldValues> {
    val tagMap = mutableMapOf<String, FieldValues>()

    rows.value.forEach { row ->
        row.cells[0].value.content?.let { key ->
            row.cells[1].value.content?.let { value ->
                var x = tagMap[key]
                if (x == null) {
                    x = FieldValues(mutableSetOf())
                    tagMap[key] = x
                }
                x.values += value
            }
        }
    }

    return tagMap
}