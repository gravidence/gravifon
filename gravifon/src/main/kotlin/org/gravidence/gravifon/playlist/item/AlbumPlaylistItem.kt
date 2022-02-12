package org.gravidence.gravifon.playlist.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("album")
data class AlbumPlaylistItem(val albumPlaylistItems: List<TrackPlaylistItem>) : PlaylistItem() {

}