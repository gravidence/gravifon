package org.gravidence.gravifon.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.behavior.PlaylistStructure
import org.gravidence.gravifon.playlist.item.PlaylistItem
import java.util.*
import kotlin.collections.ArrayDeque

@Serializable
@SerialName("queue")
class Queue(
    override val id: String = UUID.randomUUID().toString(),
    override val items: MutableList<PlaylistItem> = ArrayDeque(),
    override var position: Int = 0,
    override var playbackOrder: PlaybackOrder = PlaybackOrder.SEQUENTIAL,
    override var playlistStructure: PlaylistStructure = PlaylistStructure.TRACK
) : Playlist() {

    override fun playbackOrder(playbackOrder: PlaybackOrder) {
        // makes no effect, queue playback order is always sequential
    }

}