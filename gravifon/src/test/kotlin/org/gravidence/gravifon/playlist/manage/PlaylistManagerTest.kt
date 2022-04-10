package org.gravidence.gravifon.playlist.manage

import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.StaticPlaylist
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PlaylistManagerTest {

    private lateinit var playlistManager: PlaylistManager

    @BeforeEach
    internal fun setUp() {
        playlistManager = PlaylistManager()
    }

    @Test
    fun playCurrent_RegularPlaylist_NoSpecificPlaylistItemRequested_PriorityPlaylistIsNotEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        val pt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt2"))
        val priorityPlaylist = Queue(items = mutableListOf(pt1, pt2))

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(pt1, playlistManager.playCurrent(regularPlaylist, null))
    }

    @Test
    fun playCurrent_RegularPlaylist_NoSpecificPlaylistItemRequested_PriorityPlaylistIsEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val priorityPlaylist = Queue(items = mutableListOf())

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist, null))
    }

    @Test
    fun playCurrent_RegularPlaylist_SpecificPlaylistItemRequested_PriorityPlaylistIsNotEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        val pt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt2"))
        val priorityPlaylist = Queue(items = mutableListOf(pt1, pt2))

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist, rt1))
    }

    @Test
    fun playCurrent_RegularPlaylist_SpecificPlaylistItemRequested_PriorityPlaylistIsEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        val pt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt2"))
        val priorityPlaylist = Queue(items = mutableListOf(pt1, pt2))

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(rt2, playlistManager.playCurrent(regularPlaylist, rt2))
    }

    @Test
    fun playCurrent_PriorityPlaylist_NoSpecificPlaylistItemRequested_PriorityPlaylistIsNotEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        val pt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt2"))
        val priorityPlaylist = Queue(items = mutableListOf(pt1, pt2))

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(pt1, playlistManager.playCurrent(priorityPlaylist, null))
    }

    @Test
    fun playCurrent_PriorityPlaylist_SpecificPlaylistItemRequested_PriorityPlaylistIsNotEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        val pt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt2"))
        val priorityPlaylist = Queue(items = mutableListOf(pt1, pt2))

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(pt2, playlistManager.playCurrent(priorityPlaylist, pt2))
    }

    @Test
    fun playNext_RegularPlaylist_PriorityPlaylistIsNotEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        val pt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt2"))
        val priorityPlaylist = Queue(items = mutableListOf(pt1, pt2))

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        assertEquals(pt1, playlistManager.playNext(regularPlaylist))
    }

    @Test
    fun playNext_RegularPlaylistInactive_PriorityPlaylistIsEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val priorityPlaylist = Queue(items = mutableListOf())

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        // regular playlist isn't "activated" since no tracks were played yet

        assertEquals(rt1, playlistManager.playNext(regularPlaylist))
    }

    @Test
    fun playNext_RegularPlaylistActive_PriorityPlaylistIsEmpty() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val priorityPlaylist = Queue(items = mutableListOf())

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        // "activate" regular playlist by playing first track in current session
        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist, rt1))

        assertEquals(rt2, playlistManager.playNext(regularPlaylist))
    }

    @Test
    fun playNext_PriorityItemsInTheMiddle() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        val priorityPlaylist = Queue(items = mutableListOf())

        playlistManager.addPlaylist(regularPlaylist)
        playlistManager.addPlaylist(priorityPlaylist)

        // "activate" regular playlist by playing first track in current session
        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist, rt1))

        val pt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "pt1"))
        priorityPlaylist.append(pt1)

        assertEquals(pt1, playlistManager.playNext(regularPlaylist))

        assertEquals(rt2, playlistManager.playNext(regularPlaylist))
    }

    @Test
    fun playPrev() {
    }

    @Test
    fun getPlaylist() {
    }

    @Test
    fun testGetPlaylist() {
    }

    @Test
    fun addPlaylist() {
    }

    @Test
    fun stopAfterCurrent() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        playlistManager.addPlaylist(regularPlaylist)

        playlistManager.stopAfter(0)
        // this playback request isn't allowed
        assertNull(playlistManager.playCurrent(regularPlaylist))
        assertEquals(1, regularPlaylist.position())
        // but next try is allowed (since stopAfter is reset once occurs)
        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist))
        assertEquals(1, regularPlaylist.position())
    }

    @Test
    fun stopAfterNext() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2))

        playlistManager.addPlaylist(regularPlaylist)

        playlistManager.stopAfter(1)
        // this playback request succeeds
        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist))
        assertEquals(1, regularPlaylist.position())
        // but next one isn't allowed
        assertNull(playlistManager.playNext(regularPlaylist))
        assertEquals(2, regularPlaylist.position())
    }

    @Test
    fun stopAfterNextTwo() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val rt3 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt3"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2, rt3))

        playlistManager.addPlaylist(regularPlaylist)

        playlistManager.stopAfter(2)
        // this playback request succeeds
        assertEquals(rt1, playlistManager.playCurrent(regularPlaylist))
        assertEquals(1, regularPlaylist.position())
        // and this one
        assertEquals(rt2, playlistManager.playNext(regularPlaylist))
        assertEquals(2, regularPlaylist.position())
        // but next one isn't allowed
        assertNull(playlistManager.playNext(regularPlaylist))
        assertEquals(3, regularPlaylist.position())
    }

    @Test
    fun stopAfterPrev() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2), position = 2)

        playlistManager.addPlaylist(regularPlaylist)

        playlistManager.stopAfter(1)
        // this playback request succeeds
        assertEquals(rt2, playlistManager.playCurrent(regularPlaylist))
        assertEquals(2, regularPlaylist.position())
        // but next one isn't allowed
        assertNull(playlistManager.playPrev(regularPlaylist))
        assertEquals(1, regularPlaylist.position())
    }

    @Test
    fun stopAfterPrevTwo() {
        val rt1 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt1"))
        val rt2 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt2"))
        val rt3 = TrackPlaylistItem(TestUtil.fixedFileVirtualTrack(path = "rt3"))
        val regularPlaylist = StaticPlaylist(items = mutableListOf(rt1, rt2, rt3), position = 3)

        playlistManager.addPlaylist(regularPlaylist)

        playlistManager.stopAfter(2)
        // this playback request succeeds
        assertEquals(rt3, playlistManager.playCurrent(regularPlaylist))
        assertEquals(3, regularPlaylist.position())
        // and this one
        assertEquals(rt2, playlistManager.playPrev(regularPlaylist))
        assertEquals(2, regularPlaylist.position())
        // but next one isn't allowed
        assertNull(playlistManager.playPrev(regularPlaylist))
        assertEquals(1, regularPlaylist.position())
    }

}