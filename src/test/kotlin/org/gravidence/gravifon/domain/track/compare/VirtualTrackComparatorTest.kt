package org.gravidence.gravifon.domain.track.compare

import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class VirtualTrackComparatorTest {

    @Test
    fun createComparator_Defaults() {
        val track1 = TestUtil.fixedFileVirtualTrack(path = "b1", artist = "aaa", album = "aa1")
        val track2 = TestUtil.fixedFileVirtualTrack(path = "b2", artist = "aaa", album = "aa2")
        val track3 = TestUtil.fixedFileVirtualTrack(path = "a1", artist = "bbb", album = "bb1")
        val track4 = TestUtil.fixedFileVirtualTrack(path = "bb", artist = "aaa", album = "aab")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1, // b1
            track3, // a1
            track4, // bb
            track2, // b2
        )
        // path/uri is used as default if no selectors supplied
        val expectedTracksOrder = listOf<VirtualTrack>(
            track3, // a1
            track1, // b1
            track2, // b2
            track4, // bb
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build())

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_NoSelectors() {
        val track1 = TestUtil.fixedFileVirtualTrack(path = "b1", artist = "aaa", album = "aa1")
        val track2 = TestUtil.fixedFileVirtualTrack(path = "b2", artist = "aaa", album = "aa2")
        val track3 = TestUtil.fixedFileVirtualTrack(path = "a1", artist = "bbb", album = "bb1")
        val track4 = TestUtil.fixedFileVirtualTrack(path = "bb", artist = "aaa", album = "aab")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1, // b1
            track3, // a1
            track4, // bb
            track2, // b2
        )
        // path/uri is used as fallback if selectors list is empty
        val expectedTracksOrder = listOf<VirtualTrack>(
            track3, // a1
            track1, // b1
            track2, // b2
            track4, // bb
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf()))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Sequence() {
        val track1 = TestUtil.fixedFileVirtualTrack(artist = "Xyz", album = "Bccc", title = "111")
        val track2 = TestUtil.fixedFileVirtualTrack(artist = "Zyx", album = "Bbbb", title = "2b")
        val track3 = TestUtil.fixedFileVirtualTrack(artist = "Zyx", album = "Baaa", title = "2a")
        val track4 = TestUtil.fixedFileVirtualTrack(artist = "Xyz", album = "Baaa", title = "302")
        val track5 = TestUtil.fixedFileVirtualTrack(artist = "Xyz", album = "Baaa", title = "301")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1, // Xyz - Bccc - 111
            track2, // Zyx - Bbbb - 2b
            track3, // Zyx - Baaa - 2a
            track4, // Xyz - Baaa - 302
            track5, // Xyz - Baaa - 301
        )
        // artist/album/title selector sequence is used
        val expectedTracksOrder = listOf<VirtualTrack>(
            track5, // Xyz - Baaa - 301
            track4, // Xyz - Baaa - 302
            track1, // Xyz - Bccc - 111
            track3, // Zyx - Baaa - 2a
            track2, // Zyx - Bbbb - 2b
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(
            VirtualTrackComparator.build(
                listOf(
                    VirtualTrackSelectors.ARTIST,
                    VirtualTrackSelectors.ALBUM,
                    VirtualTrackSelectors.TITLE
                )
            )
        )

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Uri() {
        val track1 = TestUtil.fixedFileVirtualTrack(path = "bbb")
        val track2 = TestUtil.fixedFileVirtualTrack(path = "aaa")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.URI)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Artist() {
        val track1 = TestUtil.fixedFileVirtualTrack(artist = "bbb")
        val track2 = TestUtil.fixedFileVirtualTrack(artist = "aaa")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.ARTIST)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_AlbumArtist() {
        val track1 = TestUtil.fixedFileVirtualTrack(albumArtist = "bbb")
        val track2 = TestUtil.fixedFileVirtualTrack(albumArtist = "aaa")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.ALBUM_ARTIST)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Album() {
        val track1 = TestUtil.fixedFileVirtualTrack(album = "bbb")
        val track2 = TestUtil.fixedFileVirtualTrack(album = "aaa")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.ALBUM)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_FullDate() {
        val track1 = TestUtil.fixedFileVirtualTrack(date = "2020-07-19")
        val track2 = TestUtil.fixedFileVirtualTrack(date = "2020-05-14")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.DATE)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_MixedDate() {
        val track1 = TestUtil.fixedFileVirtualTrack(date = "2020-07-19")
        val track2 = TestUtil.fixedFileVirtualTrack(date = "2020")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.DATE)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_YearToDate() {
        val track1 = TestUtil.fixedFileVirtualTrack(date = "2020-07-19")
        val track2 = TestUtil.fixedFileVirtualTrack(date = "2019")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.YEAR)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_YearToYear() {
        val track1 = TestUtil.fixedFileVirtualTrack(date = "2020")
        val track2 = TestUtil.fixedFileVirtualTrack(date = "2019")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.YEAR)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Title() {
        val track1 = TestUtil.fixedFileVirtualTrack(title = "bbb")
        val track2 = TestUtil.fixedFileVirtualTrack(title = "aaa")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.TITLE)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Track() {
        val track1 = TestUtil.fixedFileVirtualTrack(track = "7")
        val track2 = TestUtil.fixedFileVirtualTrack(track = "4")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.TRACK)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_TrackTotal() {
        val track1 = TestUtil.fixedFileVirtualTrack(trackTotal = "20")
        val track2 = TestUtil.fixedFileVirtualTrack(trackTotal = "19")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.TRACK_TOTAL)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_Disc() {
        val track1 = TestUtil.fixedFileVirtualTrack(disc = "2")
        val track2 = TestUtil.fixedFileVirtualTrack(disc = "1")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.DISC)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_DiscTotal() {
        val track1 = TestUtil.fixedFileVirtualTrack(discTotal = "4")
        val track2 = TestUtil.fixedFileVirtualTrack(discTotal = "2")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.DISC_TOTAL)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_ValueToNull() {
        val track1 = TestUtil.fixedFileVirtualTrack(title = "bbb")
        val track2 = TestUtil.fixedFileVirtualTrack(title = null)

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track2,
            track1,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.TITLE)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

    @Test
    fun createComparator_NullToValue() {
        val track1 = TestUtil.fixedFileVirtualTrack(title = null)
        val track2 = TestUtil.fixedFileVirtualTrack(title = "aaa")

        val sourceTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )
        val expectedTracksOrder = listOf<VirtualTrack>(
            track1,
            track2,
        )

        val actualTracksOrder = sourceTracksOrder.sortedWith(VirtualTrackComparator.build(listOf(VirtualTrackSelectors.TITLE)))

        assertEquals(expectedTracksOrder, actualTracksOrder)
    }

}