import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.WindowStateChangedEvent
import org.gravidence.gravifon.event.playback.PausePlaybackEvent
import org.gravidence.gravifon.event.playback.RepositionPlaybackPointRelativeEvent
import org.gravidence.gravifon.ui.AppBody
import org.gravidence.gravifon.ui.dialog.ApplicationSettingsDialog
import org.gravidence.gravifon.ui.dialog.PluginSettingsDialog
import org.gravidence.gravifon.ui.dialog.TrackMetadataDialog
import kotlin.time.Duration.Companion.seconds

val windowState = readWindowState().also {
    GravifonStarter.orchestrator.startup()
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    Window(
        title = "Gravifon",
        state = windowState,
        onCloseRequest = {
            GravifonStarter.orchestrator.shutdown()

            GravifonContext.scopeDefault.cancel()
            GravifonContext.scopeIO.cancel()

            exitApplication()
        },
        onPreviewKeyEvent = {
            // TODO below doesn't work, see https://github.com/JetBrains/compose-jb/issues/1840
            if (it.key == Key.MediaPlayPause && it.type == KeyEventType.KeyUp) {
                EventBus.publish(PausePlaybackEvent())
                return@Window true
            }

            return@Window false
        }
    ) {
        LaunchedEffect(windowState) {
            snapshotFlow { windowState.size }
                .onEach { EventBus.publish(WindowStateChangedEvent(size = it)) }
                .launchIn(this)

            snapshotFlow { windowState.position }
                .filter { it.isSpecified }
                .onEach { EventBus.publish(WindowStateChangedEvent(position = it)) }
                .launchIn(this)

            snapshotFlow { windowState.placement }
                .onEach { EventBus.publish(WindowStateChangedEvent(placement = it)) }
                .launchIn(this)
        }

        MenuBar {
            Menu(text = "File") {

            }
            Menu(text = "Playback") {
                Item(
                    text = "Play/Pause",
                    shortcut = KeyShortcut(key = Key.Spacebar, shift = true),
                    onClick = { EventBus.publish(PausePlaybackEvent()) }
                )
                Separator()
                Item(
                    text = "Jump forward",
                    shortcut = KeyShortcut(key = Key.DirectionRight),
                    onClick = { EventBus.publish(RepositionPlaybackPointRelativeEvent(10.seconds)) }
                )
                Item(
                    text = "Jump backward",
                    shortcut = KeyShortcut(key = Key.DirectionLeft),
                    onClick = { EventBus.publish(RepositionPlaybackPointRelativeEvent((-10).seconds)) }
                )
                Separator()
                Item(text = "Stop after current", onClick = {})
            }
            Menu(text = "Settings") {
                Item(text = "Application...", onClick = { GravifonContext.applicationSettingsDialogVisible.value = true })
                Item(text = "Plugin...", onClick = { GravifonContext.pluginSettingsDialogVisible.value = true })
            }
            Menu(text = "View") {
                GravifonStarter.views
                    .filter { it.viewEnabled }
                    .forEach {
                        Item(
                            text = it.viewDisplayName,
                            onClick = { it.activateView() }
                        )
                    }
            }
            Menu(text = "About") {

            }
        }

        AppBody()

        ApplicationSettingsDialog()
        PluginSettingsDialog()
        TrackMetadataDialog()
    }
}

fun readWindowState(): WindowState = runBlocking {
    GravifonStarter.configurationManager.applicationConfig().window.run {
        val size = DpSize(width = size.width.dp, height = size.height.dp)
        val position = if (position.remembered && position.x != null && position.y != null) {
            WindowPosition.Absolute(x = position.x!!.dp, y = position.y!!.dp)
        } else {
            WindowPosition.PlatformDefault
        }

        if (placement.remembered) {
            WindowState(
                size = size,
                position = position,
                placement = placement.placement,
            )
        } else {
            WindowState(
                size = size,
                position = position,
            )
        }
    }
}