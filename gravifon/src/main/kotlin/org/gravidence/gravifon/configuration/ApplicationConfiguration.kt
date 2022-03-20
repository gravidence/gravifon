package org.gravidence.gravifon.configuration

import androidx.compose.ui.window.WindowPlacement
import kotlinx.serialization.Serializable

@Serializable
data class GConfig(val application: GApplication = GApplication(), val component: MutableMap<String, ComponentConfiguration> = mutableMapOf())

@Serializable
data class GApplication(
    var window: GWindow = GWindow(),
    var activeViewId: String? = null,
    var activePlaylistId: String? = null
)

@Serializable
data class GWindow(
    val position: GWindowPosition = GWindowPosition(),
    val size: GWindowSize = GWindowSize(),
    val placement: GWindowPlacement = GWindowPlacement(),
)

@Serializable
data class GWindowPosition(
    var remembered: Boolean = false,
    var x: Int? = null,
    var y: Int? = null,
)

@Serializable
data class GWindowSize(
    var width: Int = 800,
    var height: Int = 700,
)

@Serializable
data class GWindowPlacement(
    var remembered: Boolean = false,
    var placement: WindowPlacement = WindowPlacement.Floating,
)