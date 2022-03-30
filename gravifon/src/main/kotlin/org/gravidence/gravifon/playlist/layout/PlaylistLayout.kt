package org.gravidence.gravifon.playlist.layout

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistLayout(
    val columns: List<PlaylistColumn> = listOf(
        StatusColumn(width = 60, header = "Status"),
        TrackInfoColumn(width = 585, header = "Artist - Track", format = "%artist% - %title%"),
        TrackInfoColumn(width = 95, header = "Duration", format = "%duration_short%"),
    ),
)

@Serializable
sealed class PlaylistColumn {
    abstract val width: Int
    abstract val header: String
}

@Serializable
data class TrackInfoColumn(
    override val width: Int,
    override val header: String,
    val format: String,
) : PlaylistColumn()

@Serializable
data class StatusColumn(
    override val width: Int,
    override val header: String,
    val showPlaybackStatus: Boolean = true,
    val showFailureStatus: Boolean = true,
) : PlaylistColumn()