@file:OptIn(ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.ui.component.Table
import org.gravidence.gravifon.ui.component.TableColumn
import org.gravidence.gravifon.ui.component.TableGrid
import org.gravidence.gravifon.ui.component.TableLayout
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gSelectedListItemColor
import org.gravidence.gravifon.ui.theme.gShape
import java.awt.event.MouseEvent

class TrackMetadataState(val tracks: MutableState<List<VirtualTrack>>, val selectedTracks: MutableState<List<VirtualTrack>>) {

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
                size = DpSize(600.dp, 400.dp)
            ),
            onCloseRequest = {
                // TODO cleanup possible state
                GravifonContext.trackMetadataDialogVisible.value = false
            }
        ) {
            val pluginListHScrollState = rememberScrollState()
            val pluginListVScrollState = rememberScrollState()
            val pluginSettingsHScrollState = rememberScrollState()
            val pluginSettingsVScrollState = rememberScrollState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(0.4f)
                            .border(width = 1.dp, color = Color.Red, shape = gShape)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(10.dp)
//                            .horizontalScroll(state = pluginListHScrollState)
                                .verticalScroll(state = pluginListVScrollState)
                                .border(width = 1.dp, color = Color.Black, shape = gShape)
                        ) {
                            GravifonContext.trackMetadataDialogState.tracks.value.forEach {
                                trackListItem(it, GravifonContext.trackMetadataDialogState)
                            }
                        }
//                    HorizontalScrollbar(
//                        adapter = rememberScrollbarAdapter(pluginListHScrollState),
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(start = 5.dp, end = 5.dp, bottom = 2.dp)
//                    )
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(pluginListVScrollState),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(top = 5.dp, bottom = 5.dp, end = 2.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(width = 1.dp, color = Color.Magenta, shape = gShape)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(10.dp)
//                                .horizontalScroll(state = pluginSettingsHScrollState)
                                .verticalScroll(state = pluginSettingsVScrollState)
                                .border(width = 1.dp, color = Color.Black, shape = gShape)
                        ) {
                            trackContent(GravifonContext.trackMetadataDialogState)
                        }
//                        HorizontalScrollbar(
//                            adapter = rememberScrollbarAdapter(pluginSettingsHScrollState),
//                            modifier = Modifier
//                                .align(Alignment.BottomCenter)
//                                .padding(start = 5.dp, end = 5.dp, bottom = 2.dp)
//                        )
                        VerticalScrollbar(
                            adapter = rememberScrollbarAdapter(pluginSettingsVScrollState),
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(top = 5.dp, bottom = 5.dp, end = 2.dp)
                        )
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
    val grid: TableGrid = trackMetadataState.selectedTracks.value
        .flatMap { track ->
            val fields = track.fields.flatMap { tag ->
                tag.value.values.map { tagValue ->
                    mutableListOf(tag.key.name, tagValue)
                }
            }
            val customFields = track.customFields?.flatMap { tag ->
                tag.value.values.map { tagValue ->
                    mutableListOf(tag.key, tagValue)
                }
            } ?: mutableListOf()
            fields + customFields
        }
        .toMutableList()

    Table(
        layout = TableLayout(
            displayHeaders = true,
            columns = listOf(
                TableColumn(header = "Tag", fraction = 0.25f),
                TableColumn(header = "Value", fraction = 1f)
            )
        ),
//        readOnly = false,
        grid = grid
    )
}