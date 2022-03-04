package org.gravidence.gravifon.configuration

import kotlinx.serialization.Serializable

@Serializable
data class GConfig(val application: GApplication = GApplication(), val component: MutableMap<String, ComponentConfiguration> = mutableMapOf())

@Serializable
data class GApplication(var activeViewId: String? = null, var activePlaylistId: String? = null)