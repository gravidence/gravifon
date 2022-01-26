// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay
import org.gravidence.gravifon.Gravifon
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.playback.*
import org.gravidence.gravifon.event.playlist.SubPlaylistActivatePriorityPlaylistEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistActivateRegularPlaylistEvent
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayNextEvent
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.gravidence.gravifon.view.View
import org.springframework.beans.factory.getBean

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("Gravifon") }

    val view: View by remember { mutableStateOf(Gravifon.ctx.getBean<org.gravidence.gravifon.view.LibraryView>()) }

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
                            Button(onClick = {
                                EventBus.publish(SubPlaylistActivatePriorityPlaylistEvent(null))
                                EventBus.publish(SubPlaylistActivateRegularPlaylistEvent(null))
                                EventBus.publish(SubPlaylistPlayNextEvent())
                            }) {
                                Text("Play")
                            }
                            Button(onClick = {
                                EventBus.publish(SubPlaybackPauseEvent())
                            }) {
                                Text("Pause")
                            }
                            Button(onClick = {
                                EventBus.publish(SubPlaybackStopEvent())
                            }) {
                                Text("Stop")
                            }
                        }
                    )
                },
                bottomBar = {
                    Row { Text("Bottom Bar") }
                },
                content = {
//                    LibraryView()
                    view.compose()
                }
            )
        }
    }
}

@Composable
fun PlaybackInformationPanel() {

    var artist: String by remember { mutableStateOf("---") }

    EventBus.subscribe {
        when (it) {
            is PubTrackStartEvent -> {
                artist = "${it.track.getArtist()} (${it.track.getDate()}) ${it.track.getAlbum()} - ${it.track.getTitle()}" ?: "---"
            }
            is PubTrackFinishEvent -> {
                artist = "---"
            }
        }
    }

    Text(text = artist)
}

@Composable
fun PlaybackControlPanel() {

    var sliderStart: Float by remember { mutableStateOf(0f) }
    var sliderFinish: Float by remember { mutableStateOf(0f) }
    var sliderSteps: Int by remember { mutableStateOf(0) }
    var sliderPosition: Float by remember { mutableStateOf(0f) }

    EventBus.subscribe {
        when (it) {
            is PubPlaybackStartEvent -> {
                sliderStart = 0f
                sliderFinish = it.length.toFloat()
                sliderSteps = it.length.toInt()
            }
            is SubPlaybackStopEvent -> {
                sliderStart = 0f
                sliderFinish = 0f
                sliderSteps = 0
            }
            is PubPlaybackPositionEvent -> sliderPosition = it.position.toFloat()
        }
    }

    Slider(
        value = sliderPosition,
        steps = sliderSteps,
        valueRange = sliderStart..sliderFinish,
        onValueChange = {
            sliderPosition = it
            EventBus.publish(SubPlaybackPositionEvent(it.toLong()))
        })
}

@Composable
fun LibraryView() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PlaybackInformationPanel()
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                PlaybackControlPanel()
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Search Bar")
            }
            Divider()
            Row(modifier = Modifier.fillMaxWidth().fillMaxHeight(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Search Results")
                }
            }
        }
    }
}
