// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.gravidence.gravifon.Gravifon
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.SubApplicationConfigurationPersistEvent
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.gravidence.gravifon.ui.PlaybackControlComposable
import org.gravidence.gravifon.ui.rememberPlaybackControlState
import org.gravidence.gravifon.view.View
import org.springframework.beans.factory.getBean

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()

    var text by remember { mutableStateOf("Gravifon") }

    val playbackControlState = rememberPlaybackControlState()
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
                        }
                    )
                },
//                bottomBar = {
//                    Row { Text("Bottom Bar") }
//                },
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
                                PlaybackControlComposable(playbackControlState)
                            }
                            Divider(color = Color.Transparent, thickness = 5.dp)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                            ) {
                                view.compose()
                            }
                        }
                    }
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