package org.gravidence.gravifon.playlist

import kotlinx.serialization.Serializable
import org.gravidence.gravifon.playlist.behavior.LookupDirection
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.behavior.PlaylistStructure
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.playlist.layout.PlaylistLayout
import org.gravidence.gravifon.playlist.layout.ScrollPosition
import kotlin.math.max

// TODO make concurrency safe
@Serializable
sealed class Playlist {

    companion object {

        const val DEFAULT_POSITION = 0

    }

    protected abstract val id: String
    abstract val ownerName: String
    abstract var displayName: String
    protected abstract val items: MutableList<PlaylistItem>
    /**
     * Represents active playlist item position. Zero would mean playlist isn't "activated", i.e. no items played yet.
     */
    protected abstract var position: Int
    protected abstract var playbackOrder: PlaybackOrder
    protected abstract var playlistStructure: PlaylistStructure
    abstract var layout: PlaylistLayout
    abstract var verticalScrollPosition: ScrollPosition

    open fun id(): String {
        return id
    }

    fun items(): List<PlaylistItem> {
        return items.toList()
    }

    fun length(): Int {
        return items.size
    }

    open fun position(): Int {
        return position
    }

    open fun playbackOrder(): PlaybackOrder {
        return playbackOrder
    }

    open fun playbackOrder(playbackOrder: PlaybackOrder) {
        this.playbackOrder = playbackOrder
    }

    open fun structure(): PlaylistStructure {
        return playlistStructure
    }

    open fun structure(playlistStructure: PlaylistStructure) {
        this.playlistStructure = playlistStructure
        // TODO re-build playlist
    }

    open fun peekCurrent(): PlaylistItem? {
        return peekSpecific(position)
    }

    open fun peekCurrentTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(position, LookupDirection.FORWARD)
    }

    open fun moveToCurrentTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(position, LookupDirection.FORWARD)
    }

    open fun peekNext(): PlaylistItem? {
        return peekSpecific(position + 1)
    }

    open fun peekNextTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(position + 1, LookupDirection.FORWARD)
    }

    open fun moveToNextTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(position + 1, LookupDirection.FORWARD)
    }

    open fun peekPrev(): PlaylistItem? {
        return peekSpecific(position - 1)
    }

    open fun peekPrevTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(position - 1, LookupDirection.BACKWARD)
    }

    open fun moveToPrevTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(position - 1, LookupDirection.BACKWARD)
    }

    open fun peekFirst(): PlaylistItem? {
        return peekSpecific(1)
    }

    open fun peekFirstTrack(): TrackPlaylistItem? {
        return peekSpecificTrack(1, LookupDirection.FORWARD)
    }

    open fun moveToFirstTrack(): TrackPlaylistItem? {
        return moveToSpecificTrack(1, LookupDirection.FORWARD)
    }

    open fun peekSpecific(position: Int): PlaylistItem? {
        val refinedPosition = if (this.position == 0 && position < 1) {
            1
        } else {
            position
        }

        return items.elementAtOrNull(refinedPosition - 1)
    }

    open fun peekSpecificTrack(position: Int, lookupDirection: LookupDirection): TrackPlaylistItem? {
        return when (lookupDirection) {
            LookupDirection.FORWARD -> {
                val refinedPosition = if (this.position == 0 && position < 1) {
                    1
                } else {
                    position
                }

                items
                    .drop(max(0, refinedPosition - 1))
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

    open fun moveToSpecific(playlistItem: PlaylistItem): Int? {
        return items
            .indexOfFirst { it === playlistItem }
            .takeIf { it >= 0 }?.let {
                position = it + 1
                position
            }
    }

    open fun moveToSpecific(position: Int): PlaylistItem? {
        return peekSpecific(position)?.also {
            this.position = position
        }
    }

    open fun moveToSpecificTrack(position: Int, lookupDirection: LookupDirection): TrackPlaylistItem? {
        return peekSpecificTrack(position, lookupDirection)?.also { specificTrackItem ->
            this.position = items.indexOfFirst { it === specificTrackItem } + 1
        }
    }

    open fun view(): List<PlaylistItem> {
        return items.toMutableList()
    }

    open fun shuffle() {
        items.shuffle()
        rebuildStructure()
        // TODO send event PlaylistUpdated
    }

    open fun clear() {
        items.clear()
        // TODO send event PlaylistUpdated

        position = DEFAULT_POSITION
    }

    open fun append(item: PlaylistItem) {
        this.items += item
    }

    open fun append(items: List<PlaylistItem>) {
        this.items += items
    }

    open fun insert(item: PlaylistItem, position: Int) {
        TODO("Not yet implemented")
    }

    open fun insert(items: List<PlaylistItem>, position: Int) {
        TODO("Not yet implemented")
    }

    open fun prepend(item: PlaylistItem) {
        TODO("Not yet implemented")
    }

    open fun prepend(items: List<PlaylistItem>) {
        TODO("Not yet implemented")
    }

    open fun remove(item: PlaylistItem) {
        items.remove(item)
    }

    open fun remove(positionSet: Set<Int>) {
        // remove playlist items one by one to keep correct current position
        positionSet.sortedDescending().forEach { remove(it..it) }
    }

    open fun remove(positionRange: IntRange) {
        positionRange.reversed().forEach { items.removeAt(it - 1) }
        if (position in positionRange) {
            position = positionRange.first - 1
        } else if (position > positionRange.last) {
            position -= positionRange.count()
        }
    }

    open fun replace(items: List<PlaylistItem>) {
        clear()
        append(items)
    }

    private fun normalisePosition(position: Int): Int {
        return position.coerceIn(1, max(1, items.size))
    }

    protected open fun rebuildStructure() {
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