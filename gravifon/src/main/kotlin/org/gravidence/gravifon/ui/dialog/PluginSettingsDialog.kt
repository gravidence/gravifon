package org.gravidence.gravifon.ui.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.plugin.Plugin
import java.awt.event.MouseEvent


class PluginSettingsState(val selectedPlugin: MutableState<Plugin?>) {

    fun onPointerEvent(pointerEvent: PointerEvent, plugin: Plugin) {
        (pointerEvent.nativeEvent as? MouseEvent)?.apply {
            if (button == 1) {
                selectedPlugin.value = plugin
            }
        }
    }

}

@Composable
fun rememberPluginSettingsState(
    selectedPlugin: MutableState<Plugin?> = mutableStateOf(GravifonStarter.plugins.firstOrNull()),
) = remember(selectedPlugin) { PluginSettingsState(selectedPlugin) }

@Composable
fun PluginSettingsDialog() {
    val pluginSettingsState = rememberPluginSettingsState()

    Dialog(
        title = "Plugin Settings",
        visible = GravifonContext.pluginSettingsDialogVisible.value, // wrap dialog with if-clause to dispose it after closure
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(600.dp, 400.dp)
        ),
        onCloseRequest = { GravifonContext.pluginSettingsDialogVisible.value = false }
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
                        .border(width = 1.dp, color = Color.Red, shape = RoundedCornerShape(5.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(10.dp)
//                            .horizontalScroll(state = pluginListHScrollState)
                            .verticalScroll(state = pluginListVScrollState)
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                    ) {
                        GravifonStarter.plugins.forEach {
                            pluginListItem(it, pluginSettingsState)
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
                        .border(width = 1.dp, color = Color.Magenta, shape = RoundedCornerShape(5.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
//                            .horizontalScroll(state = pluginSettingsHScrollState)
                            .verticalScroll(state = pluginSettingsVScrollState)
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                    ) {
                        pluginContent(pluginSettingsState)
                    }
//                    HorizontalScrollbar(
//                        adapter = rememberScrollbarAdapter(pluginSettingsHScrollState),
//                        modifier = Modifier
//                            .align(Alignment.BottomCenter)
//                            .padding(start = 5.dp, end = 5.dp, bottom = 2.dp)
//                    )
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun pluginListItem(plugin: Plugin, pluginSettingsState: PluginSettingsState) {
    var baseModifier = Modifier
        .padding(5.dp)
    if (plugin == pluginSettingsState.selectedPlugin.value) {
        baseModifier = baseModifier
            .background(color = Color.LightGray, shape = RoundedCornerShape(5.dp))
    }

    Row(
        modifier = baseModifier
            .fillMaxWidth()
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
            .onPointerEvent(
                eventType = PointerEventType.Release,
                onEvent = {
                    pluginSettingsState.onPointerEvent(it, plugin)
                }
            )
    ) {
        Text(
            text = plugin.title,
            modifier = Modifier
                .padding(5.dp)
        )
    }
}

@Composable
fun pluginContent(pluginSettingsState: PluginSettingsState) {
    pluginSettingsState.selectedPlugin.value?.let { plugin ->
        Box {
            Column {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Text(plugin.description)
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                )
                Row(
                    modifier = Modifier
                        .padding(10.dp)
                ) {
                    plugin.composeSettings()
                }
            }
        }
    }
}