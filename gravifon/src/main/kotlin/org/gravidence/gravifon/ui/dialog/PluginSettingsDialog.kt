@file:OptIn(ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui.dialog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.ui.component.*
import org.gravidence.gravifon.ui.theme.gShape

class PluginListState(
    private val pluginSettingsState: PluginSettingsState,
) : TableState<Plugin>(
    layout = layout(),
    multiSelection = mutableStateOf(false),
    grid = grid(pluginSettingsState),
    selectedRows = selectedPlugin(pluginSettingsState)
) {

    override fun onRowClick(rowIndex: Int, pointerEvent: PointerEvent) {
        super.onRowClick(rowIndex, pointerEvent)
        pluginSettingsState.selectedPlugin.value = selectedRows.value.firstOrNull()?.let { pluginSettingsState.plugins[it] }
    }

    companion object {

        fun layout(): MutableState<TableLayout> {
            return mutableStateOf(
                TableLayout(
                    displayHeaders = false,
                    columns = listOf(
                        TableColumn(header = "Plugin", fraction = 1f)
                    )
                )
            )
        }

        fun grid(pluginSettingsState: PluginSettingsState): MutableState<TableGrid<Plugin>?> {
            return mutableStateOf(singleColumnTableGrid(
                pluginSettingsState.plugins.map {
                    TableCell(content = it.pluginDisplayName)
                }
            ))
        }

        fun selectedPlugin(pluginSettingsState: PluginSettingsState): MutableState<Set<Int>> {
            val plugin = pluginSettingsState.selectedPlugin.value
            return if (plugin != null) {
                mutableStateOf(setOf(pluginSettingsState.plugins.indexOf(plugin)))
            } else {
                mutableStateOf(setOf())
            }
        }

    }

}

class PluginSettingsState(
    val plugins: List<Plugin>,
    val selectedPlugin: MutableState<Plugin?>
)

@Composable
fun rememberPluginSettingsState(
    plugins: List<Plugin> = GravifonStarter.plugins.toList(),
    selectedPlugin: MutableState<Plugin?> = mutableStateOf(GravifonStarter.plugins.firstOrNull()),
) = remember(selectedPlugin) { PluginSettingsState(plugins, selectedPlugin) }

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
                        pluginListItem(pluginSettingsState)
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
//                            .horizontalScroll(state = pluginSettingsHScrollState)
                            .verticalScroll(state = pluginSettingsVScrollState)
                            .border(width = 1.dp, color = Color.Black, shape = gShape)
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

@Composable
fun pluginListItem(pluginSettingsState: PluginSettingsState) {
    Table(PluginListState(pluginSettingsState))
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
                    Text(plugin.pluginDescription)
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