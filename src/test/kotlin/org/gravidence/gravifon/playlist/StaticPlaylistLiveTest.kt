package org.gravidence.gravifon.playlist

import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class StaticPlaylistLiveTest {

    private val album1track1 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Second Bad Vilbel",
            title = "Second Bad Vilbel",
            artist = "Autechre",
            album = "Anvil Vapre",
            albumArtist = "Autechre"
        )
    )
    private val album1track2 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Second Scepe",
            title = "Second Scepe",
            artist = "Autechre",
            album = "Anvil Vapre",
            albumArtist = "Autechre"
        )
    )
    private val album1track3 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Second Scout",
            title = "Second Scout",
            artist = "Autechre",
            album = "Anvil Vapre",
            albumArtist = "Autechre"
        )
    )
    private val album1track4 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Second Peng",
            title = "Second Peng",
            artist = "Autechre",
            album = "Anvil Vapre",
            albumArtist = "Autechre"
        )
    )
    private val album1 = AlbumPlaylistItem(listOf(album1track1, album1track2, album1track3, album1track4))

    private val album2track1 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Gantz Graf",
            title = "Gantz Graf",
            artist = "Autechre",
            album = "Gantz Graf",
            albumArtist = "Autechre"
        )
    )
    private val album2track2 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Dial.",
            title = "Dial.",
            artist = "Autechre",
            album = "Gantz Graf",
            albumArtist = "Autechre"
        )
    )
    private val album2track3 = TrackPlaylistItem(
        TestUtil.fixedFileVirtualTrack(
            path = "Cap.IV",
            title = "Cap.IV",
            artist = "Autechre",
            album = "Gantz Graf",
            albumArtist = "Autechre"
        )
    )
    private val album2 = AlbumPlaylistItem(listOf(album2track1, album2track2, album2track3))

    @Test
    fun moveToNextTrack_FromStartSequentially() {
        val playlist = StaticPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 0
        )

        assertEquals(album1track1, playlist.moveToNextTrack())
        assertEquals(1, playlist.position())
        assertEquals(4, playlist.length())

        assertEquals(album1track2, playlist.moveToNextTrack())
        assertEquals(2, playlist.position())
        assertEquals(4, playlist.length())

        assertEquals(album1track3, playlist.moveToNextTrack())
        assertEquals(3, playlist.position())
        assertEquals(4, playlist.length())

        assertEquals(album1track4, playlist.moveToNextTrack())
        assertEquals(4, playlist.position())
        assertEquals(4, playlist.length())
    }

    @Test
    fun moveToNextTrack_AlbumPlaylist_RemoveAfterCurrent() {
        val playlist = StaticPlaylist(
            items = mutableListOf(
                album1,
                album1track1,
                album1track2,
                album1track3,
                album1track4,
                album2,
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        playlist.moveToNextTrack()
        assertEquals(album1track2, playlist.peekCurrentTrack())

        playlist.remove(7..9)

        playlist.moveToNextTrack()
        assertEquals(album1track3, playlist.peekCurrentTrack())
    }

    @Test
    fun moveToNextTrack_AlbumPlaylist_RemoveBeforeCurrent() {
        val playlist = StaticPlaylist(
            items = mutableListOf(
                album1,
                album1track1,
                album1track2,
                album1track3,
                album1track4,
                album2,
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 3
        )

        playlist.moveToNextTrack()
        assertEquals(album1track3, playlist.peekCurrentTrack())

        playlist.remove(2..2)

        playlist.moveToNextTrack()
        assertEquals(album1track4, playlist.peekCurrentTrack())
    }

    @Test
    fun moveToNextTrack_AlbumPlaylist_RemoveJustCurrent() {
        val playlist = StaticPlaylist(
            items = mutableListOf(
                album1,
                album1track1,
                album1track2,
                album1track3,
                album1track4,
                album2,
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 3
        )

        playlist.moveToNextTrack()
        assertEquals(album1track3, playlist.peekCurrentTrack())

        playlist.remove(4..4)

        playlist.moveToNextTrack()
        assertEquals(album1track4, playlist.peekCurrentTrack())
    }

    @Test
    fun moveToNextTrack_TrackPlaylist_RemoveWithCurrent() {
        val playlist = StaticPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        playlist.moveToNextTrack()
        assertEquals(album1track3, playlist.peekCurrentTrack())

        playlist.remove(1..4)

        playlist.moveToNextTrack()
        assertEquals(album2track1, playlist.peekCurrentTrack())
    }

}