package org.gravidence.gravifon.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.behavior.PlaylistStructure
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.playlist.layout.PlaylistLayout
import org.gravidence.gravifon.playlist.layout.ScrollPosition
import java.util.*
import kotlin.collections.ArrayDeque

@Serializable
@SerialName("queue")
class Queue(
    override val id: String = UUID.randomUUID().toString(),
    override val ownerName: String = "Owner Name",
    override var displayName: String = "Display Name",
    override val items: MutableList<PlaylistItem> = ArrayDeque(),
    override var position: Int = DEFAULT_POSITION,
    override var playbackOrder: PlaybackOrder = PlaybackOrder.SEQUENTIAL,
    override var playlistStructure: PlaylistStructure = PlaylistStructure.TRACK,
    override var layout: PlaylistLayout = PlaylistLayout(),
    override var verticalScrollPosition: ScrollPosition = ScrollPosition(),
) : Playlist() {

    override fun playbackOrder(playbackOrder: PlaybackOrder) {
        // makes no effect, queue playback order is always sequential
    }

    override fun structure(playlistStructure: PlaylistStructure) {
        // makes no effect, queue structure is always flat (tracks only)
    }

    override fun moveToNextTrack(): TrackPlaylistItem? {
        val nextTrack = super.moveToNextTrack()
        if (nextTrack != null) {
            while (peekFirst() != nextTrack) {
                remove(1..1)
            }
        } else {
            clear()
        }
        return nextTrack
    }

    override fun peekPrev(): PlaylistItem? {
        return super.peekCurrent()
    }

    override fun peekPrevTrack(): TrackPlaylistItem? {
        return super.peekCurrentTrack()
    }

    override fun moveToPrevTrack(): TrackPlaylistItem? {
        return super.moveToCurrentTrack()
    }

    override fun clear() {
        super.clear()
        position = 0
    }

}