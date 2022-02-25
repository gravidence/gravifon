@file:OptIn(ExperimentalComposeUiApi::class)

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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.tag.FieldValue
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.ui.component.*
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gSelectedListItemColor
import org.gravidence.gravifon.ui.theme.gShape
import org.jaudiotagger.tag.FieldKey
import java.awt.event.MouseEvent

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

    fun onPointerEvent(pointerEvent: PointerEvent, track: VirtualTrack) {
        (pointerEvent.nativeEvent as? MouseEvent)?.apply {
            if (button == 1) {
                selectedTracks.value = listOf(track)
            }
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
                                GravifonContext.trackMetadataDialogState.tracks.value.forEach {
                                    trackListItem(it, GravifonContext.trackMetadataDialogState)
                                }
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
fun trackListItem(track: VirtualTrack, trackMetadataState: TrackMetadataState) {
    val normalTrackListItemModifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .background(color = gListItemColor, shape = gShape)
    val selectedTrackListItemModifier = Modifier
        .fillMaxWidth()
        .padding(5.dp)
        .background(color = gSelectedListItemColor, shape = gShape)

    val trackListItemModifier = if (track in trackMetadataState.selectedTracks.value) {
        selectedTrackListItemModifier
    } else {
        normalTrackListItemModifier
    }

    Row(
        modifier = trackListItemModifier
            .onPointerEvent(
                eventType = PointerEventType.Release,
                onEvent = {
                    trackMetadataState.onPointerEvent(it, track)
                }
            )
    ) {
        Text(
            text = track.uri().toString(),
            maxLines = 1,
            overflow = TextOverflow.Clip,
            modifier = Modifier
                .padding(5.dp)
        )
    }
}

@Composable
fun trackContent(trackMetadataState: TrackMetadataState) {
    val grid = buildTableGrid(trackMetadataState.selectedTracks.value)

    Table(
        layout = TableLayout(
            displayHeaders = true,
            columns = listOf(
                TableColumn(header = "Tag", fraction = 0.25f),
                TableColumn(header = "Value", fraction = 1f)
            )
        ),
        readOnly = false,
        grid = grid,
        onCellChange = { rowIndex, columnIndex, newValue ->
            grid?.let {
                it[rowIndex][columnIndex].value = newValue
                trackMetadataState.changed.value = true
            }
        }
    )
}

private fun buildTableGrid(tracks: List<VirtualTrack>): TableGrid? {
    return when (tracks.size) {
        0 -> null
        1 -> buildTableGridForSingleTrack(tracks.first())
        else -> null
    }
}

private fun buildTableGridForSingleTrack(track: VirtualTrack): TableGrid {
    val fields = track.fields.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            mutableListOf(mutableStateOf(fieldKey.name), mutableStateOf(fieldValue))
        }
    }
    val customFields = track.customFields?.flatMap { (fieldKey, fieldValues) ->
        fieldValues.values.map { fieldValue ->
            mutableListOf(mutableStateOf(fieldKey), mutableStateOf(fieldValue))
        }
    } ?: listOf()

    return (fields + customFields).toMutableList()
}