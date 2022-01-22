package org.gravidence.gravifon.domain.album

import org.gravidence.gravifon.TestUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.time.Duration

internal class AlbumScannerTest {

    @Test
    fun fullScan() {
        val originalAlbums = TestUtil.manyRandomVirtualAlbums(10)
        val allTracksShuffled = originalAlbums.flatMap { it.tracks }.shuffled()
        val discoveredAlbums = AlbumScanner.fullScan(allTracksShuffled)

        assertEquals(
            originalAlbums.sortedBy { it.albumKey }.onEach { it.tracks.sortBy { it.getTitle() } },
            discoveredAlbums.sortedBy { it.albumKey }.onEach { it.tracks.sortBy { it.getTitle() } }
        )
    }

    @Test
    fun fullScanNoAlbumTitleButDifferentAlbumArtists() {
        val album1Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")

        val album2Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", title = "The Track", albumArtist = "Artist 2")

        val album3Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", title = "The Track (Artist 3 Remix)", albumArtist = "Artist 3")

        val allTracksShuffled = listOf(
            album1Track1,
            album1Track2,
            album2Track1,
            album3Track2,
        ).shuffled()

        val discoveredAlbums = AlbumScanner.fullScan(allTracksShuffled)

        /*
        albums:
        - (Album) Artist 1 / Greatest Hits
        - NO_ALBUM_KEY
         */
        assertEquals(2, discoveredAlbums.size)

        assertTrue(discoveredAlbums.any { it.tracks.containsAll(listOf(album1Track1, album1Track2)) })
        assertTrue(discoveredAlbums.any { it.tracks.containsAll(listOf(album2Track1, album3Track2)) })
    }

    @Test
    fun fullScanSameAlbumTitleButDifferentAlbumArtists() {
        val album1Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 2", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track4 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track5 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 3", album = "Greatest Hits", albumArtist = "Artist 1")

        val album2Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")
        val album2Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")
        val album2Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")
        val album2Track4 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")
        val album2Track5 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")

        val allTracksShuffled = listOf(
            album1Track1,
            album1Track2,
            album1Track3,
            album1Track4,
            album1Track5,
            album2Track1,
            album2Track2,
            album2Track3,
            album2Track4,
            album2Track5,
        ).shuffled()

        val discoveredAlbums = AlbumScanner.fullScan(allTracksShuffled)

        /*
        albums:
        - (Album) Artist 1 / Greatest Hits
        - (Album) Artist 2 / Greatest Hits
         */
        assertEquals(2, discoveredAlbums.size)

        assertTrue(discoveredAlbums.any { it.tracks.containsAll(listOf(album1Track1, album1Track2, album1Track3, album1Track4, album1Track5)) })
        assertTrue(discoveredAlbums.any { it.tracks.containsAll(listOf(album2Track1, album2Track2, album2Track3, album2Track4, album2Track5)) })
    }

    @Test
    fun fullScanSameAlbumTitleAndNoAlbumArtists() {
        val album1Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 2", album = "Greatest Hits")
        val album1Track4 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track5 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 3", album = "Greatest Hits")

        val album2Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")
        val album2Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")
        val album2Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")
        val album2Track4 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")
        val album2Track5 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")

        val allTracksShuffled = listOf(
            album1Track1,
            album1Track2,
            album1Track3,
            album1Track4,
            album1Track5,
            album2Track1,
            album2Track2,
            album2Track3,
            album2Track4,
            album2Track5,
        ).shuffled()

        val discoveredAlbums = AlbumScanner.fullScan(allTracksShuffled)

        /*
        albums:
        - Greatest Hits
         */
        assertEquals(1, discoveredAlbums.size)

        assertTrue(discoveredAlbums.any { it.tracks.containsAll(allTracksShuffled) })
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun fullScanBigCollection() {
        val originalAlbums = TestUtil.manyRandomVirtualAlbums(10000)
        val allTracksShuffled = originalAlbums.flatMap { it.tracks }.shuffled()

        assertTimeoutPreemptively(Duration.ofMillis(150)) {
            AlbumScanner.fullScan(allTracksShuffled)
        }
    }

    @Test
    fun slidingScan() {
        val album1Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 2", album = "Greatest Hits")
        val album1Track4 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track5 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 3", album = "Greatest Hits")

        val album2Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Album 2")
        val album2Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Album 2")

        val allTracksScientificallyShuffled = listOf(
            album1Track1,
            album1Track2,
            album1Track3,
            album2Track1,
            album2Track2,
            album1Track4,
            album1Track5,
        )

        /*
        albums:
        - Greatest Hits
        - Album 2
        - Greatest Hits
         */
        val discoveredAlbums = AlbumScanner.slidingScan(allTracksScientificallyShuffled)

        assertEquals(3, discoveredAlbums.size)

        assertEquals(listOf(album1Track1, album1Track2, album1Track3), discoveredAlbums[0].tracks)
        assertEquals(listOf(album2Track1, album2Track2), discoveredAlbums[1].tracks)
        assertEquals(listOf(album1Track4, album1Track5), discoveredAlbums[2].tracks)
    }

    @Test
    fun slidingScanSameAlbumTitleButDifferentAlbumArtists() {
        val album1Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits", albumArtist = "Artist 1")
        val album1Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 2", album = "Greatest Hits", albumArtist = "Artist 1")

        val album2Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")
        val album2Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits", albumArtist = "Artist 2")

        val allTracksScientificallyShuffled = mutableListOf(
            album1Track1,
            album1Track2,
            album2Track1,
            album2Track2,
            album1Track3,
        )

        val discoveredAlbums = AlbumScanner.slidingScan(allTracksScientificallyShuffled)

        /*
        albums:
        - (Album) Artist 1 / Greatest Hits
        - (Album) Artist 2 / Greatest Hits
        - (Album) Artist 1 / Greatest Hits
         */
        assertEquals(3, discoveredAlbums.size)

        assertEquals(listOf(album1Track1, album1Track2), discoveredAlbums[0].tracks)
        assertEquals(listOf(album2Track1, album2Track2), discoveredAlbums[1].tracks)
        assertEquals(listOf(album1Track3), discoveredAlbums[2].tracks)
    }

    @Test
    fun slidingScanSameAlbumTitleAndNoAlbumArtists() {
        val album1Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1", album = "Greatest Hits")
        val album1Track3 = TestUtil.fixedFileVirtualTrack(artist = "Artist 1 & Artist 2", album = "Greatest Hits")

        val album2Track1 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")
        val album2Track2 = TestUtil.fixedFileVirtualTrack(artist = "Artist 2", album = "Greatest Hits")

        val allTracksScientificallyShuffled = mutableListOf(
            album1Track1,
            album1Track2,
            album1Track3,
            album2Track1,
            album2Track2,
        )

        val discoveredAlbums = AlbumScanner.slidingScan(allTracksScientificallyShuffled)

        /*
        albums:
        - Greatest Hits
         */
        assertEquals(1, discoveredAlbums.size)

        assertEquals(allTracksScientificallyShuffled, discoveredAlbums[0].tracks)
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun slidingScanBigCollection() {
        val originalAlbums = TestUtil.manyRandomVirtualAlbums(10000)
        val allTracksShuffled = originalAlbums.flatMap { it.tracks }.shuffled()

        assertTimeoutPreemptively(Duration.ofMillis(150)) {
            AlbumScanner.slidingScan(allTracksShuffled)
        }
    }

    @Test
    fun calculateAlbumKey() {
        assertEquals("Calling All The People", AlbumScanner.calculateAlbumKey(
            TestUtil.fixedFileVirtualTrack(artist = "Artist X", album = "Calling All The People", albumArtist = null)))
        assertEquals("Calling All The People::Various", AlbumScanner.calculateAlbumKey(
            TestUtil.fixedFileVirtualTrack(artist = "Artist X", album = "Calling All The People", albumArtist = "Various")))
    }

    @Test
    fun calculateAlbumKeyNoAlbum() {
        assertEquals(NO_ALBUM_KEY, AlbumScanner.calculateAlbumKey(
            TestUtil.fixedFileVirtualTrack(artist = "Artist X", album = null, albumArtist = null)))
        assertEquals(NO_ALBUM_KEY, AlbumScanner.calculateAlbumKey(
            TestUtil.fixedFileVirtualTrack(artist = "Artist X", album = null, albumArtist = "Artist X")))
    }

}