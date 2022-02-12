// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.cancel
import org.gravidence.gravifon.GravifonStarter
import org.gravidence.gravifon.GravifonContext

fun main() = application {
    Window(
        onCloseRequest = {
            GravifonStarter.orchestrator.shutdown()

            GravifonContext.scopeDefault.cancel()
            GravifonContext.scopeIO.cancel()

            exitApplication()
        },
        title = "Gravifon"
    ) {
        GravifonStarter.kickoff()
        App()
    }
}