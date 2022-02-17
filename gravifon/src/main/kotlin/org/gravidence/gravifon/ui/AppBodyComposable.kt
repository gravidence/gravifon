package org.gravidence.gravifon.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import kotlin.concurrent.fixedRateTimer

@Composable
fun AppBody() {
    var text by remember { mutableStateOf("Gravifon") }

    val playbackInformationState = rememberPlaybackInformationState()
    val playbackControlState = rememberPlaybackControlState()
    val contextInformationState = rememberContextInformationState()
    val activeView = remember { GravifonContext.activeView }

    val contextInformationTimer = fixedRateTimer(initialDelay = 1000, period = 1000) {
        contextInformationState.refresh()
    }

    LaunchedEffect(Unit) {
        delay(2000)
        text += " "
        for (i in 1..10) {
            delay(200)
            text += ">"
        }
    }

    MaterialTheme {
        Column {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = text) },
                        actions = {
                            Button(onClick = {
                                EventBus.publish(SubApplicationConfigurationPersistEvent())
                            }) {
                                Text("Save")
                            }
                        }
                    )
                },
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(5.dp)
                    ) {
                        Column() {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                            ) {
                                PlaybackInformationComposable(playbackInformationState)
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                            ) {
                                PlaybackControlComposable(playbackControlState)
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                            ) {
                                val value = activeView.value
                                if (value == null) {
                                    // TODO make proper initialization indicator
                                    Text("Initialization...")
                                } else {
                                    value.composeView()
                                }
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                            ) {
                                ContextInformationComposable(contextInformationState)
                            }
                        }
                    }
                }
            )
        }
    }
}