import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.key
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playback.SubPlaybackPauseEvent
import org.gravidence.gravifon.event.playback.SubPlaybackRelativePositionEvent
import org.gravidence.gravifon.ui.AppBody
import org.gravidence.gravifon.ui.dialog.PluginSettingsDialog
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalComposeUiApi::class)
fun main() = application {
    GravifonContext.scopeDefault.launch {
        GravifonStarter.orchestrator.startup()
    }

    Window(
        title = "Gravifon",
        onCloseRequest = {
            GravifonStarter.orchestrator.shutdown()

            GravifonContext.scopeDefault.cancel()
            GravifonContext.scopeIO.cancel()

            exitApplication()
        },
        onPreviewKeyEvent = {
            // TODO below doesn't work, see https://github.com/JetBrains/compose-jb/issues/1840
            if (it.key == Key.MediaPlayPause) {
                EventBus.publish(SubPlaybackPauseEvent())
                return@Window true
            }

            return@Window false
        }
    ) {
        MenuBar {
            Menu(text = "File") {

            }
            Menu(text = "Playback") {
                Item(
                    text = "Play/Pause",
                    shortcut = KeyShortcut(key = Key.Spacebar, shift = true),
                    onClick = { EventBus.publish(SubPlaybackPauseEvent()) }
                )
                Separator()
                Item(
                    text = "Jump forward",
                    shortcut = KeyShortcut(key = Key.DirectionRight),
                    onClick = { EventBus.publish(SubPlaybackRelativePositionEvent(10.seconds)) }
                )
                Item(
                    text = "Jump backward",
                    shortcut = KeyShortcut(key = Key.DirectionLeft),
                    onClick = { EventBus.publish(SubPlaybackRelativePositionEvent((-10).seconds)) }
                )
                Separator()
                Item(text = "Stop after current", onClick = {})
            }
            Menu(text = "Settings") {
                Item(text = "Application...", onClick = {})
                Item(text = "Plugin...", onClick = { GravifonContext.pluginSettingsDialogVisible.value = true })
            }
            Menu(text = "View") {
                GravifonStarter.views.forEach {
                    Item(
                        text = it.viewDisplayName(),
                        onClick = { it.activate() }
                    )
                }
            }
            Menu(text = "About") {

            }
        }

        AppBody()

        PluginSettingsDialog()
    }
}