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
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.ui.component.*
import org.gravidence.gravifon.ui.theme.gShape
import org.jaudiotagger.tag.FieldKey

class TrackMetadataState(
    val tracks: MutableState<List<VirtualTrack>>,
    val selectedTracks: MutableState<List<VirtualTrack>>,
    val changed: MutableState<Boolean> = mutableStateOf(false)
) {

    fun clear() {
        tracks.value = mutableListOf()
        selectedTracks.value = mutableListOf()
        changed.value = false
    }

}

class TrackMetadataListState(
    private val trackMetadataState: TrackMetadataState,
) : TableState(
    layout = layout(),
    readOnly = mutableStateOf(true),
    grid = grid(trackMetadataState)
) {

    override fun onRowClick(rowIndex: Int, pointerEvent: PointerEvent) {
        super.onRowClick(rowIndex, pointerEvent)
        trackMetadataState.selectedTracks.value = selectedRows.value.map { trackMetadataState.tracks.value[it] }
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

        fun grid(trackMetadataState: TrackMetadataState): MutableState<TableGrid?> {
            return mutableStateOf(singleColumnTableGrid(trackMetadataState.tracks.value.map { it.uri().toString() }))
        }

    }

}

class TrackMetadataTableState(
    private val trackMetadataState: TrackMetadataState,
) : TableState(
    layout = layout(),
    enabled = mutableStateOf(true),
    readOnly = mutableStateOf(false),
    grid = grid(trackMetadataState)
) {

    override fun onCellChange(rowIndex: Int, columnIndex: Int, newValue: String) {
        super.onCellChange(rowIndex, columnIndex, newValue)
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

        fun grid(trackMetadataState: TrackMetadataState): MutableState<TableGrid?> {
            return mutableStateOf(buildTableGrid(trackMetadataState.selectedTracks.value))
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
                                    onClick = {}
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

private fun buildTableGrid(tracks: List<VirtualTrack>): TableGrid? {
    return when (tracks.size) {
        0 -> null
        1 -> buildTableGridForSingleTrack(tracks.first())
        else -> buildTableGridForManyTracks(tracks)
    }
}

private fun buildTableGridForSingleTrack(track: VirtualTrack): TableGrid {
    val fieldRows = track.fields.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            TableRow(cells = mutableListOf(mutableStateOf(fieldKey.name), mutableStateOf(fieldValue)))
        }
    }
    val customFieldRows = track.customFields?.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            TableRow(cells = mutableListOf(mutableStateOf(fieldKey), mutableStateOf(fieldValue)))
        }
    } ?: listOf()

    return TableGrid(
        rows = mutableStateOf((fieldRows + customFieldRows).toMutableList())
    )
}

private fun buildTableGridForManyTracks(tracks: List<VirtualTrack>): TableGrid {
    val map = mutableMapOf<FieldKey, FieldValue?>()
    tracks.forEach { track ->
        track.fields.forEach { (fieldKey, fieldValues) ->
            fieldValues.values.forEach { fieldValue ->
                if (map.containsKey(fieldKey)) {
                    val prevFieldValue = map[fieldKey]
                    if (prevFieldValue != null && prevFieldValue != fieldValue) {
                        map[fieldKey] = null
                    }
                } else {
                    map[fieldKey] = fieldValue
                }
            }
        }
    }

    return TableGrid(
        rows = mutableStateOf(
            map.map {
                TableRow(cells = mutableListOf(mutableStateOf(it.key.name), mutableStateOf(it.value)))
            }.toMutableList()
        )
    )
}