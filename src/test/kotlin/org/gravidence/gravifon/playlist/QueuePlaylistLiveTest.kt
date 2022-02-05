package org.gravidence.gravifon.playlist

import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class QueuePlaylistLiveTest {

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

    @Test
    fun moveToNextTrack_FromStartSequentially() {
        val playlist = Queue(
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
        assertEquals(1, playlist.position())
        assertEquals(3, playlist.length())

        assertEquals(album1track3, playlist.moveToNextTrack())
        assertEquals(1, playlist.position())
        assertEquals(2, playlist.length())

        assertEquals(album1track4, playlist.moveToNextTrack())
        assertEquals(1, playlist.position())
        assertEquals(1, playlist.length())

        assertNull(playlist.moveToNextTrack())
        assertEquals(0, playlist.position())
        assertEquals(0, playlist.length())
    }

    @Test
    fun moveToNextTrack_RemoveCurrent() {
        val playlist = Queue(
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

        playlist.remove(1..1)
        assertEquals(0, playlist.position())
        assertEquals(3, playlist.length())

        assertEquals(album1track2, playlist.moveToNextTrack())
        assertEquals(1, playlist.position())
        assertEquals(3, playlist.length())
    }

}