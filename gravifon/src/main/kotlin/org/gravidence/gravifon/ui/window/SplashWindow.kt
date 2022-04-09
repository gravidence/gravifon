package org.gravidence.gravifon.ui.window

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState

@Composable
fun SplashWindow() {
    Window(
        title = "Gravifon",
        state = WindowState(size = DpSize(200.dp, 100.dp), position = WindowPosition(Alignment.Center)),
        resizable = false,
        undecorated = true,
        onCloseRequest = { },
    ) {
        WindowDraggableArea {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                modifier = Modifier
                    .requiredWidth(200.dp)
                    .requiredHeight(100.dp)
                    .background(Color.Blue.copy(alpha = 0.1f))
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Text("Boot Gravifon...")
                LinearProgressIndicator()
            }
        }
    }
}