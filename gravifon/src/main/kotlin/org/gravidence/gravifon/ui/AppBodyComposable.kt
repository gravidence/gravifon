package org.gravidence.gravifon.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.ui.theme.gShape
import kotlin.concurrent.fixedRateTimer

@Composable
fun AppBody() {
    val playbackInformationState = rememberPlaybackInformationState()
    val playbackControlState = rememberPlaybackControlState()
    val contextInformationState = rememberContextInformationState()
    val activeView = remember { GravifonContext.activeView }

    val contextInformationTimer = fixedRateTimer(initialDelay = 1000, period = 1000) {
        contextInformationState.refresh()
    }

    MaterialTheme {
        Column {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Gravifon")
                        },
                        actions = { }
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
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
                            ) {
                                PlaybackInformationComposable(playbackInformationState)
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
                            ) {
                                PlaybackControlComposable(playbackControlState)
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
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
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
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