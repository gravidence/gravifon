package org.gravidence.gravifon.playlist

import org.gravidence.gravifon.playlist.behavior.LookupDirection
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.behavior.PlaylistStructure
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import java.util.*

interface Playlist {

    fun id(): UUID
    fun position(): Int
    fun playbackOrder(): PlaybackOrder
    fun playbackOrder(playbackOrder: PlaybackOrder)
    fun structure(): PlaylistStructure
    fun structure(playlistStructure: PlaylistStructure)

    fun peekCurrent(): PlaylistItem?
    fun peekCurrentTrack(): TrackPlaylistItem?
    fun moveToCurrentTrack(): TrackPlaylistItem?
    fun peekNext(): PlaylistItem?
    fun peekNextTrack(): TrackPlaylistItem?
    fun moveToNextTrack(): TrackPlaylistItem?
    fun peekPrev(): PlaylistItem?
    fun peekPrevTrack(): TrackPlaylistItem?
    fun moveToPrevTrack(): TrackPlaylistItem?
    fun peekFirst(): PlaylistItem?
    fun peekFirstTrack(): TrackPlaylistItem?
    fun moveToFirstTrack(): TrackPlaylistItem?
    fun peekSpecific(position: Int): PlaylistItem?
    fun peekSpecificTrack(position: Int, lookupDirection: LookupDirection): TrackPlaylistItem?
    fun moveToSpecific(position: Int): PlaylistItem?
    fun moveToSpecificTrack(position: Int, lookupDirection: LookupDirection): TrackPlaylistItem?

    fun init(
        items: List<PlaylistItem>,
        position: Int = 1,
        playbackOrder: PlaybackOrder = PlaybackOrder.SEQUENTIAL,
        playlistStructure: PlaylistStructure = PlaylistStructure.TRACK
    )
    fun view(): List<PlaylistItem>
    fun shuffle()
    fun clear()

    fun append(item: PlaylistItem)
    fun append(items: List<PlaylistItem>)
    fun insert(item: PlaylistItem, position: Int)
    fun insert(items: List<PlaylistItem>, position: Int)
    fun prepend(item: PlaylistItem)
    fun prepend(items: List<PlaylistItem>)
    fun remove(item: PlaylistItem)
    fun remove(positionRange: IntRange)

}