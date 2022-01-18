package org.gravidence.gravifon.playlist

import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.item.PlaylistItem
import java.util.*

class FixedPlaylist(
    id: UUID = UUID.randomUUID(),
    items: MutableList<PlaylistItem> = ArrayList(),
    position: Int = 1,
    playbackOrder: PlaybackOrder = PlaybackOrder.SEQUENTIAL
) : GenericPlaylist(id, items, position, playbackOrder) {

}