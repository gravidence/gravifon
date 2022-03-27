package org.gravidence.gravifon.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.behavior.PlaylistStructure
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.layout.PlaylistLayout
import org.gravidence.gravifon.playlist.layout.ScrollPosition
import java.util.*

@Serializable
@SerialName("dynamic")
class DynamicPlaylist(
    override val id: String = UUID.randomUUID().toString(),
    override val ownerName: String = "Owner Name",
    override var displayName: String = "Display Name",
    override val items: MutableList<PlaylistItem> = ArrayList(),
    override var position: Int = DEFAULT_POSITION,
    override var playbackOrder: PlaybackOrder = PlaybackOrder.SEQUENTIAL,
    override var playlistStructure: PlaylistStructure = PlaylistStructure.TRACK,
    override var layout: PlaylistLayout = PlaylistLayout(),
    override var verticalScrollPosition: ScrollPosition = ScrollPosition(),
) : Playlist()