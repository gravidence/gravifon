import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.launch
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.ui.window.ApplicationWindow
import org.gravidence.gravifon.ui.window.SplashWindow

fun main() = application {
    var isLoading by remember { mutableStateOf(true) }
    var windowState by remember { mutableStateOf(WindowState()) }

    if (isLoading) {
        SplashWindow()

        GravifonContext.scopeDefault.launch {
            windowState = resolveWindowState()
            GravifonStarter.orchestrator.startup()
            isLoading = false
        }
    } else {
        ApplicationWindow(windowState)
    }
}

fun resolveWindowState(): WindowState {
    return GravifonStarter.configurationManager.applicationConfig().window.run {
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