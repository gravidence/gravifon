// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import org.gravidence.gravifon.domain.FileVirtualTrack
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.ApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.playback.PlaybackPauseEvent
import org.gravidence.gravifon.event.playback.PlaybackStartEvent
import org.gravidence.gravifon.event.playback.PlaybackStopEvent

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("Gravifon") }

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
                                EventBus.publish(ApplicationConfigurationPersistEvent())
                            }) {
                                Text("Save")
                            }
                            Button(onClick = {
                                EventBus.publish(PlaybackStartEvent(FileVirtualTrack("start")))
                            }) {
                                Text("Play")
                            }
                            Button(onClick = {
                                EventBus.publish(PlaybackPauseEvent(FileVirtualTrack("pause")))
                            }) {
                                Text("Pause")
                            }
                            Button(onClick = {
                                EventBus.publish(PlaybackStopEvent(FileVirtualTrack("stop")))
                            }) {
                                Text("Stop")
                            }
                        }
                    )
                },
                content = {
                    Column {
                    }
                }
            )
        }
    }
}

