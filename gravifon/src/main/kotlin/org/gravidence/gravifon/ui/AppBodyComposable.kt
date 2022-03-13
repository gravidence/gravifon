@file:OptIn(ExperimentalFoundationApi::class)

package org.gravidence.gravifon.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.ui.theme.gShape

@Composable
fun AppBody() {
    MaterialTheme {
        Column {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Gravifon / ${GravifonContext.activeView.value?.viewDisplayName}")
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
                                TooltipArea(
                                    delayMillis = 1000,
                                    tooltip = {
                                        GravifonContext.activeTrackExtraInfo.value.apply {
                                            if (isNotEmpty()) {
                                                tooltip(joinToString(System.lineSeparator()))
                                            }
                                        }
                                    }
                                ) {
                                    PlaybackInformationComposable(rememberPlaybackInformationState())
                                }
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
                            ) {
                                PlaybackControlComposable()
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
                            ) {
                                ActiveView()
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = gShape)
                            ) {
                                ContextInformationComposable()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun ActiveView() {
    val activeView = GravifonContext.activeView.value

    if (activeView == null) {
        // TODO make proper initialization indicator
        Text("Initialization...")
    } else {
        activeView.composeView()
    }
}