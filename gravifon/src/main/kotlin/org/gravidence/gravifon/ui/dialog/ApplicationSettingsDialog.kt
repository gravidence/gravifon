package org.gravidence.gravifon.ui.dialog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberDialogState
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playback.SelectAudioBackendEvent
import org.gravidence.gravifon.playback.backend.AudioBackend
import org.gravidence.gravifon.ui.component.DropdownField
import org.gravidence.gravifon.ui.component.DropdownFieldState
import org.gravidence.gravifon.ui.theme.gShape

@Composable
fun ApplicationSettingsDialog() {
    val tabs = remember {
        listOf(
            SoundTab(),
        )
    }

    var selectedTabIndex by remember { mutableStateOf(0) }

    Dialog(
        title = "Application Settings",
        visible = GravifonContext.applicationSettingsDialogVisible.value, // wrap dialog with if-clause to dispose it after closure
        state = rememberDialogState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(800.dp, 500.dp)
        ),
        onCloseRequest = { GravifonContext.applicationSettingsDialogVisible.value = false }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .border(width = 1.dp, color = Color.Black)
        ) {
            Column {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    tabs = {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = index == selectedTabIndex,
                                text = { Text(tab.title) },
                                onClick = { selectedTabIndex = index }
                            )
                        }
                    }
                )
                Row {
                    tabs[selectedTabIndex].composeTab()
                }
            }
        }
    }
}

interface ApplicationSettingsTab {

    val title: String

    @Composable
    fun composeTab()

}

class SoundTab(
    override val title: String = "Sound",
    private val audioBackends: List<AudioBackend> = GravifonStarter.audioBackends.toList(),
) : ApplicationSettingsTab {

    @Composable
    override fun composeTab() {
        val dropdownFieldState = remember {
            DropdownFieldState(
                label = "Audio Backend",
                items = audioBackends.map { it.id }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .border(width = 1.dp, color = Color.Black, shape = gShape)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .padding(10.dp)
            ) {
                Box{
                    BackendSelector(dropdownFieldState)
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    BackendSettings(dropdownFieldState)
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 10.dp)
                )
                Box {
                    BackendActions(dropdownFieldState)
                }
            }
        }
    }

    @Composable
    private fun BackendSelector(dropdownFieldState: DropdownFieldState) {
        DropdownField(dropdownFieldState)
    }

    @Composable
    private fun BackendSettings(dropdownFieldState: DropdownFieldState) {
        audioBackends[dropdownFieldState.selectedItemIndex.value].composeSettings()
    }

    @Composable
    private fun BackendActions(dropdownFieldState: DropdownFieldState) {
        // audio backend actually used for playback
        val activeAudioBackendId = mutableStateOf(GravifonStarter.configurationManager.applicationConfig().activeAudioBackendId)
        // audio backend selected within the dialog, to adjust its settings or activate
        val selectedAudioBackend = audioBackends[dropdownFieldState.selectedItemIndex.value]

        Row(
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp, alignment = Alignment.End),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 10.dp)
        ) {
            Button(
                enabled = selectedAudioBackend.id != activeAudioBackendId.value,
                onClick = {
                    EventBus.publish(SelectAudioBackendEvent(selectedAudioBackend))
                    // force recomposition
                    // potentially unsafe, as local activeAudioBackendId variable may become out of sync if event processing fails
                    activeAudioBackendId.value = selectedAudioBackend.id
                }
            ) {
                Text("Switch Audio Backend")
            }
        }
    }

}