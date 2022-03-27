package org.gravidence.gravifon.playlist.layout

import kotlinx.serialization.Serializable

@Serializable
data class ScrollPosition(
    val index: Int = 0,
    val scrollOffset: Int = 0,
)