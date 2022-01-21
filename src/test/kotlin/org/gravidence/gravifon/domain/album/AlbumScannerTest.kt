package org.gravidence.gravifon.domain.album

import kotlinx.datetime.Clock
import org.gravidence.gravifon.TestUtil
import org.jaudiotagger.tag.FieldKey
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable

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
    fun fullScanNoAlbumIdentified() {
        val noAlbumTrack1 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }
        val noAlbumTrack2 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }
        val noAlbumTrack3 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }
        val noAlbumTrack4 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }

        val theAlbumTitle = "the album"
        val theAlbumTrack1 = TestUtil.randomFileVirtualTrack(theAlbumTitle)
        val theAlbumTrack2 = TestUtil.randomFileVirtualTrack(theAlbumTitle)
        val theAlbumTrack3 = TestUtil.randomFileVirtualTrack(theAlbumTitle)

        val allTracksScientificallyShuffled = mutableListOf(
            noAlbumTrack1,
            noAlbumTrack2,
            theAlbumTrack1,
            noAlbumTrack3,
            noAlbumTrack4,
            theAlbumTrack2,
            theAlbumTrack3,
        )
        val discoveredAlbums = AlbumScanner.fullScan(allTracksScientificallyShuffled)

        assertEquals(2, discoveredAlbums.size)

        val theAlbum = discoveredAlbums.find { it.albumKey == theAlbumTitle }
        assertNotNull(theAlbum)
        assertTrue(theAlbum!!.tracks.containsAll(
            listOf(theAlbumTrack1, theAlbumTrack2, theAlbumTrack3)))

        val noAlbum = discoveredAlbums.find { it.albumKey == "" }
        assertNotNull(noAlbum)
        assertTrue(noAlbum!!.tracks.containsAll(
            listOf(noAlbumTrack1, noAlbumTrack2, noAlbumTrack3, noAlbumTrack4)))
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun fullScanBigCollection() {
        val numberOfAlbums = 10000
        val originalAlbums = TestUtil.manyRandomVirtualAlbums(numberOfAlbums)
        val allTracksShuffled = originalAlbums.flatMap { it.tracks }.shuffled()

        val start = Clock.System.now()
        AlbumScanner.fullScan(allTracksShuffled)
        val finish = Clock.System.now()

        val duration = finish.minus(start).inWholeMilliseconds
        println("Full scan duration against $numberOfAlbums albums (or ${allTracksShuffled.size} tracks) took ${duration}ms")
        assertTrue(duration < 100)
    }

    @Test
    fun slidingScan() {
        val album1Title = "album1"
        val album1Track1 = TestUtil.randomFileVirtualTrack(album1Title)
        val album1Track2 = TestUtil.randomFileVirtualTrack(album1Title)
        val album1Track3 = TestUtil.randomFileVirtualTrack(album1Title)
        val album1Track4 = TestUtil.randomFileVirtualTrack(album1Title)

        val noAlbumTrack1 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }
        val noAlbumTrack2 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }
        val noAlbumTrack3 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }
        val noAlbumTrack4 = TestUtil.randomFileVirtualTrack().also { it.clearField(FieldKey.ALBUM) }

        val album2Title = "album2"
        val album2Track1 = TestUtil.randomFileVirtualTrack(album2Title)
        val album2Track2 = TestUtil.randomFileVirtualTrack(album2Title)
        val album2Track3 = TestUtil.randomFileVirtualTrack(album2Title)

        val allTracksScientificallyShuffled = mutableListOf(
            // album1
            album1Track1,
            album1Track2,
            album1Track3,
            album1Track4,
            // no album
            noAlbumTrack1,
            noAlbumTrack2,
            // album2
            album2Track1,
            // no album
            noAlbumTrack3,
            noAlbumTrack4,
            // album2
            album2Track2,
            album2Track3,
        )
        val discoveredAlbums = AlbumScanner.slidingScan(allTracksScientificallyShuffled)

        assertEquals(5, discoveredAlbums.size)

        assertEquals(album1Title, discoveredAlbums[0].albumKey)
        assertEquals(4, discoveredAlbums[0].tracks.size)
        assertTrue(discoveredAlbums[0].tracks.containsAll(
            listOf(album1Track1, album1Track2, album1Track3, album1Track4)))

        assertEquals("", discoveredAlbums[1].albumKey)
        assertEquals(2, discoveredAlbums[1].tracks.size)
        assertTrue(discoveredAlbums[1].tracks.containsAll(
            listOf(noAlbumTrack1, noAlbumTrack2)))

        assertEquals(album2Title, discoveredAlbums[2].albumKey)
        assertEquals(1, discoveredAlbums[2].tracks.size)
        assertTrue(discoveredAlbums[2].tracks.containsAll(
            listOf(album2Track1)))

        assertEquals("", discoveredAlbums[3].albumKey)
        assertEquals(2, discoveredAlbums[3].tracks.size)
        assertTrue(discoveredAlbums[3].tracks.containsAll(
            listOf(noAlbumTrack3, noAlbumTrack4)))

        assertEquals(album2Title, discoveredAlbums[4].albumKey)
        assertEquals(2, discoveredAlbums[4].tracks.size)
        assertTrue(discoveredAlbums[4].tracks.containsAll(
            listOf(album2Track2, album2Track3)))
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun slidingScanBigCollection() {
        val numberOfAlbums = 10000
        val originalAlbums = TestUtil.manyRandomVirtualAlbums(numberOfAlbums)
        val allTracksShuffled = originalAlbums.flatMap { it.tracks }.shuffled()

        val start = Clock.System.now()
        AlbumScanner.slidingScan(allTracksShuffled)
        val finish = Clock.System.now()

        val duration = finish.minus(start).inWholeMilliseconds
        println("Sliding scan duration against $numberOfAlbums albums (or ${allTracksShuffled.size} tracks) took ${duration}ms")
        assertTrue(duration < 100)
    }

}