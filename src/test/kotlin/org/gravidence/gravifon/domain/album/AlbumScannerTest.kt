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
            originalAlbums.sortedBy { it.album }.onEach { it.tracks.sortBy { it.getTitle() } },
            discoveredAlbums.sortedBy { it.album }.onEach { it.tracks.sortBy { it.getTitle() } }
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

        val theAlbum = discoveredAlbums.find { it.album == theAlbumTitle }
        assertNotNull(theAlbum)
        assertTrue(theAlbum!!.tracks.containsAll(
            listOf(theAlbumTrack1, theAlbumTrack2, theAlbumTrack3)))

        val noAlbum = discoveredAlbums.find { it.album == "" }
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
        println("Full scan duration over $numberOfAlbums albums (or ${allTracksShuffled.size} tracks) took ${duration}ms")
        assertTrue(duration < 100)
    }

    @Test
    fun slidingScan() {
    }

}