package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable
import org.gravidence.gravifon.plugin.library.Library

@Serializable
data class GConfig(val application: GApplication = GApplication(), val component: MutableMap<String, ComponentConfiguration> = mutableMapOf())

@Serializable
data class GApplication(var activeViewId: String = Library::class.qualifiedName!!, var activePlaylistId: String? = null)