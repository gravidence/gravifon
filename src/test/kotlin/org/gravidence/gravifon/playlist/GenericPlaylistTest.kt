package org.gravidence.gravifon.playlist

import org.gravidence.gravifon.domain.track.FileVirtualTrack
import org.gravidence.gravifon.playlist.behavior.LookupDirection
import org.gravidence.gravifon.playlist.behavior.PlaybackOrder
import org.gravidence.gravifon.playlist.item.AlbumPlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.*

internal class GenericPlaylistTest {

    private val album1track1 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Second Bad Vilbel",
//            title = "Second Bad Vilbel",
//            artist = "Autechre",
//            album = "Anvil Vapre",
//            albumartist = "Autechre"
        )
    )
    private val album1track2 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Second Scepe",
//            title = "Second Scepe",
//            artist = "Autechre",
//            album = "Anvil Vapre",
//            albumartist = "Autechre"
        )
    )
    private val album1track3 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Second Scout",
//            title = "Second Scout",
//            artist = "Autechre",
//            album = "Anvil Vapre",
//            albumartist = "Autechre"
        )
    )
    private val album1track4 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Second Peng",
//            title = "Second Peng",
//            artist = "Autechre",
//            album = "Anvil Vapre",
//            albumartist = "Autechre"
        )
    )
    private val album1 = AlbumPlaylistItem(listOf(album1track1, album1track2, album1track3, album1track4))

    private val album2track1 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Gantz Graf",
//            title = "Gantz Graf",
//            artist = "Autechre",
//            album = "Gantz Graf",
//            albumartist = "Autechre"
        )
    )
    private val album2track2 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Dial.",
//            title = "Dial.",
//            artist = "Autechre",
//            album = "Gantz Graf",
//            albumartist = "Autechre"
        )
    )
    private val album2track3 = TrackPlaylistItem(
        FileVirtualTrack(
            path = "Cap.IV",
//            title = "Cap.IV",
//            artist = "Autechre",
//            album = "Gantz Graf",
//            albumartist = "Autechre"
        )
    )
    private val album2 = AlbumPlaylistItem(listOf(album2track1, album2track2, album2track3))

    @Test
    fun id() {
        val expectedId = UUID.randomUUID()
        val playlist = GenericPlaylist(id = expectedId)
        assertEquals(expectedId, playlist.id())
    }

    @Test
    fun peekCurrent_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.peekCurrent()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track3, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekCurrent_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.peekCurrent()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2, actualPlaylistItem)

        assertEquals(6, playlist.position())
    }

    @Test
    fun peekCurrent_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekCurrent()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekCurrentTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.peekCurrentTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track2, actualPlaylistItem)

        assertEquals(2, playlist.position())
    }

    @Test
    fun peekCurrentTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            )
        )

        val actualPlaylistItem = playlist.peekCurrentTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekCurrentTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekCurrentTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToCurrentTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.moveToCurrentTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track3, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun moveToCurrentTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToCurrentTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(7, playlist.position())
    }

    @Test
    fun moveToCurrentTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.moveToCurrentTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToCurrentTrack_TailOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.moveToCurrentTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track3, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekNext_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.peekNext()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekNext_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.peekNext()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(6, playlist.position())
    }

    @Test
    fun peekNext_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekNext()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekNext_TailOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 4
        )

        val actualPlaylistItem = playlist.peekNext()
        assertNull(actualPlaylistItem)

        assertEquals(4, playlist.position())
    }

    @Test
    fun peekNextTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.peekNextTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track3, actualPlaylistItem)

        assertEquals(2, playlist.position())
    }

    @Test
    fun peekNextTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            )
        )

        val actualPlaylistItem = playlist.peekNextTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekNextTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekNextTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekNextTrack_TailOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 4
        )

        val actualPlaylistItem = playlist.peekNextTrack()
        assertNull(actualPlaylistItem)

        assertEquals(4, playlist.position())
    }

    @Test
    fun moveToNextTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            )
        )

        val actualPlaylistItem = playlist.moveToNextTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track2, actualPlaylistItem)

        assertEquals(2, playlist.position())
    }

    @Test
    fun moveToNextTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToNextTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(7, playlist.position())
    }

    @Test
    fun moveToNextTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.moveToNextTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToNextTrack_TailOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 4
        )

        val actualPlaylistItem = playlist.moveToNextTrack()
        assertNull(actualPlaylistItem)

        assertEquals(4, playlist.position())
    }

    @Test
    fun peekPrev_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.peekPrev()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track2, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekPrev_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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

        val actualPlaylistItem = playlist.peekPrev()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1, actualPlaylistItem)

        assertEquals(2, playlist.position())
    }

    @Test
    fun peekPrev_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekPrev()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekPrev_HeadOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 1
        )

        val actualPlaylistItem = playlist.peekPrev()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekPrevTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 4
        )

        val actualPlaylistItem = playlist.peekPrevTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track3, actualPlaylistItem)

        assertEquals(4, playlist.position())
    }

    @Test
    fun peekPrevTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 7
        )

        val actualPlaylistItem = playlist.peekPrevTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(7, playlist.position())
    }

    @Test
    fun peekPrevTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekPrevTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekPrevTrack_HeadOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 1
        )

        val actualPlaylistItem = playlist.peekPrevTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToPrevTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.moveToPrevTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToPrevTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToPrevTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(5, playlist.position())
    }

    @Test
    fun moveToPrevTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.moveToPrevTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToPrevTrack_HeadOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            )
        )

        val actualPlaylistItem = playlist.moveToPrevTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekFirst_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.peekFirst()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekFirst_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            )
        )

        val actualPlaylistItem = playlist.peekFirst()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekFirst_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekFirst()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekFirstTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 4
        )

        val actualPlaylistItem = playlist.peekFirstTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(4, playlist.position())
    }

    @Test
    fun peekFirstTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 7
        )

        val actualPlaylistItem = playlist.peekFirstTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(7, playlist.position())
    }

    @Test
    fun peekFirstTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.peekFirstTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekFirstTrack_HeadOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 1
        )

        val actualPlaylistItem = playlist.peekFirstTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToFirstTrack_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.moveToFirstTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToFirstTrack_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToFirstTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(2, playlist.position())
    }

    @Test
    fun moveToFirstTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        val actualPlaylistItem = playlist.moveToFirstTrack()
        assertNull(actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToFirstTrack_HeadOfPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            )
        )

        val actualPlaylistItem = playlist.moveToFirstTrack()
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun peekSpecific_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        // first
        val actualPlaylistItem1 = playlist.peekSpecific(1)
        assertNotNull(actualPlaylistItem1)
        assertEquals(album1track1, actualPlaylistItem1)

        assertEquals(3, playlist.position())

        // some other
        val actualPlaylistItem2 = playlist.peekSpecific(2)
        assertNotNull(actualPlaylistItem2)
        assertEquals(album1track2, actualPlaylistItem2)

        assertEquals(3, playlist.position())

        // self
        val actualPlaylistItem3 = playlist.peekSpecific(3)
        assertNotNull(actualPlaylistItem3)
        assertEquals(album1track3, actualPlaylistItem3)

        assertEquals(3, playlist.position())

        // last
        val actualPlaylistItem4 = playlist.peekSpecific(4)
        assertNotNull(actualPlaylistItem4)
        assertEquals(album1track4, actualPlaylistItem4)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekSpecific_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 4
        )

        // first
        val actualPlaylistItem1 = playlist.peekSpecific(1)
        assertNotNull(actualPlaylistItem1)
        assertEquals(album1, actualPlaylistItem1)

        assertEquals(4, playlist.position())

        // some other
        val actualPlaylistItem8 = playlist.peekSpecific(8)
        assertNotNull(actualPlaylistItem8)
        assertEquals(album2track2, actualPlaylistItem8)

        assertEquals(4, playlist.position())

        // self
        val actualPlaylistItem4 = playlist.peekSpecific(4)
        assertNotNull(actualPlaylistItem4)
        assertEquals(album1track3, actualPlaylistItem4)

        assertEquals(4, playlist.position())

        // last
        val actualPlaylistItem9 = playlist.peekSpecific(9)
        assertNotNull(actualPlaylistItem9)
        assertEquals(album2track3, actualPlaylistItem9)

        assertEquals(4, playlist.position())
    }

    @Test
    fun peekSpecific_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        assertNull(playlist.peekSpecific(0))
        assertEquals(1, playlist.position())

        assertNull(playlist.peekSpecific(1))
        assertEquals(1, playlist.position())

        assertNull(playlist.peekSpecific(10))
        assertEquals(1, playlist.position())
    }

    @Test
    fun peekSpecificTrack_TrackPlaylist_Forward() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.peekSpecificTrack(4, LookupDirection.FORWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekSpecificTrack_TrackPlaylist_Backward() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album1track1,
                album1track2,
                album1track3,
                album1track4,
            ),
            position = 3
        )

        val actualPlaylistItem = playlist.peekSpecificTrack(4, LookupDirection.BACKWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun peekSpecificTrack_AlbumPlaylist_Forward() {
        val playlist = GenericPlaylist(
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
            position = 5
        )

        val actualPlaylistItem = playlist.peekSpecificTrack(6, LookupDirection.FORWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(5, playlist.position())
    }

    @Test
    fun peekSpecificTrack_AlbumPlaylist_Backward() {
        val playlist = GenericPlaylist(
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
            position = 5
        )

        val actualPlaylistItem = playlist.peekSpecificTrack(6, LookupDirection.BACKWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(5, playlist.position())
    }

    @Test
    fun peekSpecificTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        assertNull(playlist.peekSpecificTrack(0, LookupDirection.FORWARD))
        assertEquals(1, playlist.position())
        assertNull(playlist.peekSpecificTrack(0, LookupDirection.BACKWARD))
        assertEquals(1, playlist.position())

        assertNull(playlist.peekSpecificTrack(1, LookupDirection.FORWARD))
        assertEquals(1, playlist.position())
        assertNull(playlist.peekSpecificTrack(1, LookupDirection.BACKWARD))
        assertEquals(1, playlist.position())

        assertNull(playlist.peekSpecificTrack(2, LookupDirection.FORWARD))
        assertEquals(1, playlist.position())
        assertNull(playlist.peekSpecificTrack(2, LookupDirection.BACKWARD))
        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToSpecific_TrackPlaylist() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.moveToSpecific(1)
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToSpecific_AlbumPlaylist() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToSpecific(1)
        assertNotNull(actualPlaylistItem)
        assertEquals(album1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToSpecific_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        assertNull(playlist.moveToSpecific(0))
        assertEquals(1, playlist.position())
        assertNull(playlist.moveToSpecific(0))
        assertEquals(1, playlist.position())

        assertNull(playlist.moveToSpecific(1))
        assertEquals(1, playlist.position())
        assertNull(playlist.moveToSpecific(1))
        assertEquals(1, playlist.position())

        assertNull(playlist.moveToSpecific(5))
        assertEquals(1, playlist.position())
        assertNull(playlist.moveToSpecific(5))
        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToSpecificTrack_TrackPlaylist_Forward() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.moveToSpecificTrack(1, LookupDirection.FORWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track1, actualPlaylistItem)

        assertEquals(1, playlist.position())
    }

    @Test
    fun moveToSpecificTrack_TrackPlaylist_Backward() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 2
        )

        val actualPlaylistItem = playlist.moveToSpecificTrack(3, LookupDirection.BACKWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album2track3, actualPlaylistItem)

        assertEquals(3, playlist.position())
    }

    @Test
    fun moveToSpecificTrack_AlbumPlaylist_Forward() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToSpecificTrack(1, LookupDirection.FORWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track1, actualPlaylistItem)

        assertEquals(2, playlist.position())
    }

    @Test
    fun moveToSpecificTrack_AlbumPlaylist_Backward() {
        val playlist = GenericPlaylist(
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
            position = 6
        )

        val actualPlaylistItem = playlist.moveToSpecificTrack(6, LookupDirection.BACKWARD)
        assertNotNull(actualPlaylistItem)
        assertEquals(album1track4, actualPlaylistItem)

        assertEquals(5, playlist.position())
    }

    @Test
    fun moveToSpecificTrack_EmptyPlaylist() {
        val playlist = GenericPlaylist()

        assertNull(playlist.moveToSpecificTrack(0, LookupDirection.FORWARD))
        assertEquals(1, playlist.position())
        assertNull(playlist.moveToSpecificTrack(0, LookupDirection.BACKWARD))
        assertEquals(1, playlist.position())

        assertNull(playlist.moveToSpecificTrack(1, LookupDirection.FORWARD))
        assertEquals(1, playlist.position())
        assertNull(playlist.moveToSpecificTrack(1, LookupDirection.BACKWARD))
        assertEquals(1, playlist.position())

        assertNull(playlist.moveToSpecificTrack(5, LookupDirection.FORWARD))
        assertEquals(1, playlist.position())
        assertNull(playlist.moveToSpecificTrack(5, LookupDirection.BACKWARD))
        assertEquals(1, playlist.position())
    }

    @Test
    fun init() {
        val expectedItems = mutableListOf(
            album1,
            album1track1,
            album1track2,
            album1track3,
            album1track4,
            album2,
            album2track1,
            album2track2,
            album2track3,
        )

        val playlist = GenericPlaylist()
        playlist.init(
            items = expectedItems,
            position = 4,
            playbackOrder = PlaybackOrder.REPEAT_PLAYLIST
        )

        assertEquals(expectedItems, playlist.view())
        assertEquals(4, playlist.position())
        assertEquals(PlaybackOrder.REPEAT_PLAYLIST, playlist.playbackOrder())
    }

    @Test
    fun shuffle() {
        val expectedItems = mutableListOf(
            album1,
            album1track1,
            album1track2,
            album1track3,
            album1track4,
            album2,
            album2track1,
            album2track2,
            album2track3,
            // same tracks twice
            album2track1,
            album2track2,
            album2track3,
        )

        val playlist = GenericPlaylist()
        playlist.init(items = expectedItems)

        playlist.shuffle()

        assertEquals(expectedItems.size, playlist.view().size)
        assertNotEquals(expectedItems, playlist.view())
        assertTrue(expectedItems.containsAll(playlist.view()))
        // TODO check how album playlist items are re-built
    }

    @Test
    fun clear() {
        val playlist = GenericPlaylist(
            items = mutableListOf(
                album2track1,
                album2track2,
                album2track3,
            ),
            position = 3
        )

        playlist.clear()

        assertEquals(1, playlist.position())
        assertTrue(playlist.view().isEmpty())
    }

}