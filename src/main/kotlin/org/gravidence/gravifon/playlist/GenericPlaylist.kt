package org.gravidence.gravifon.playlist

import org.gravidence.gravifon.playlist.behavior.LookupDirection
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.behavior.PlaylistStructure
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import java.util.*
import kotlin.math.max

// TODO make concurrency safe
open class GenericPlaylist(
    private val id: UUID = UUID.randomUUID(),
    private val items: MutableList<PlaylistItem> = ArrayList(),
    private var position: Int = 1,
    private var playbackOrder: PlaybackOrder = PlaybackOrder.SEQUENTIAL,
    private var playlistStructure: PlaylistStructure = PlaylistStructure.TRACK,
) : Playlist {

    override fun id(): UUID {
        return id
    }

    override fun position(): Int {
        return position
    }

    override fun playbackOrder(): PlaybackOrder {
        return playbackOrder
    }

    override fun playbackOrder(playbackOrder: PlaybackOrder) {
        this.playbackOrder = playbackOrder
    }

    override fun structure(): PlaylistStructure {
        return playlistStructure
    }

    override fun structure(playlistStructure: PlaylistStructure) {
        this.playlistStructure = playlistStructure
        // TODO re-build playlist
    }

    override fun peekCurrent(): PlaylistItem? {
        return peekSpecific(position)
    }

    override fun peekCurrentTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(position, LookupDirection.FORWARD)
    }

    override fun moveToCurrentTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(position, LookupDirection.FORWARD)
    }

    override fun peekNext(): PlaylistItem? {
        return peekSpecific(position + 1)
    }

    override fun peekNextTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(position + 1, LookupDirection.FORWARD)
    }

    override fun moveToNextTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(position + 1, LookupDirection.FORWARD)
    }

    override fun peekPrev(): PlaylistItem? {
        return peekSpecific(position - 1)
    }

    override fun peekPrevTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(position - 1, LookupDirection.BACKWARD)
    }

    override fun moveToPrevTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(position - 1, LookupDirection.BACKWARD)
    }

    override fun peekFirst(): PlaylistItem? {
        return peekSpecific(1)
    }

    override fun peekFirstTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(1, LookupDirection.FORWARD)
    }

    override fun moveToFirstTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(1, LookupDirection.FORWARD)
    }

    override fun peekSpecific(position: Int): PlaylistItem? {
        return items.elementAtOrNull(position - 1)
    }

    override fun peekSpecificTrack(position: Int, lookupDirection: LookupDirection): TrackPlaylistItem? {
        return when (lookupDirection) {
            LookupDirection.FORWARD -> {
                items
                    .drop(max(0, position - 1))
                    .filterIsInstance<TrackPlaylistItem>()
                    .firstOrNull()
            }
            LookupDirection.BACKWARD -> {
                items.asReversed()
                    .drop(max(0, items.size - position))
                    .filterIsInstance<TrackPlaylistItem>()
                    .firstOrNull()
            }
        }
    }

    override fun moveToSpecific(position: Int): PlaylistItem? {
        val playlistItem = peekSpecific(position)

        if (playlistItem != null) {
            this.position = position
        }

        return playlistItem
    }

    override fun moveToSpecificTrack(position: Int, lookupDirection: LookupDirection): TrackPlaylistItem? {
        val playlistItem = peekSpecificTrack(position, lookupDirection)

        if (playlistItem != null) {
            this.position = items.indexOf(playlistItem) + 1
        }

        return playlistItem
    }

    override fun init(items: List<PlaylistItem>, position: Int, playbackOrder: PlaybackOrder, playlistStructure: PlaylistStructure) {
        this.items.clear()
        this.items.addAll(items)
        // TODO re-build playlist

        this.position = normalisePosition(position)
        this.playbackOrder = playbackOrder
        this.playlistStructure = playlistStructure
    }

    override fun view(): List<PlaylistItem> {
        return items.toMutableList()
    }

    override fun shuffle() {
        items.shuffle()
        rebuildStructure()
        // TODO send event PlaylistUpdated
    }

    override fun clear() {
        items.clear()
        // TODO send event PlaylistUpdated

        position = 1
    }

    override fun append(item: PlaylistItem) {
        TODO("Not yet implemented")
    }

    override fun append(items: List<PlaylistItem>) {
        TODO("Not yet implemented")
    }

    override fun insert(item: PlaylistItem, position: Int) {
        TODO("Not yet implemented")
    }

    override fun insert(items: List<PlaylistItem>, position: Int) {
        TODO("Not yet implemented")
    }

    override fun prepend(item: PlaylistItem) {
        TODO("Not yet implemented")
    }

    override fun prepend(items: List<PlaylistItem>) {
        TODO("Not yet implemented")
    }

    override fun remove(item: PlaylistItem) {
        TODO("Not yet implemented")
    }

    override fun remove(positionRange: IntRange) {
        TODO("Not yet implemented")
    }

    private fun normalisePosition(position: Int): Int {
        return position.coerceIn(1, max(1, items.size))
    }

    protected fun rebuildStructure() {
        when (playlistStructure) {
            PlaylistStructure.ALBUM -> {
                items.removeAll { it is AlbumPlaylistItem }

                // TODO implement me
                var lastAlbumPlaylistItem: AlbumPlaylistItem
            }
            else -> {}
        }
    }

}