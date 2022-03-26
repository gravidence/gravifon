package org.gravidence.gravifon.playlist.layout

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistLayout(
    val columns: List<PlaylistColumn> = listOf(
        PlaylistColumn(width = 600, header = "Artist - Track", format = "%artist% - %title%"),
        PlaylistColumn(width = 150, header = "Duration", format = "%duration_short%"),
    ),
)

@Serializable
data class PlaylistColumn(
    val width: Int,
    val header: String,
    val format: String,
)