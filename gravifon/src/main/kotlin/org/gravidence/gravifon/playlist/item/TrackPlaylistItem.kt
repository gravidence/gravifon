package org.gravidence.gravifon.playlist.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.track.VirtualTrack

@Serializable
@SerialName("track")
data class TrackPlaylistItem(val track: VirtualTrack) : PlaylistItem() {
}