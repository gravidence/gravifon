package org.gravidence.gravifon.playback.backend.gstreamer

import org.gravidence.gravifon.TestUtil
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class TrackQueueTest {

    private lateinit var trackQueue: TrackQueue

    @BeforeEach
    internal fun setUp() {
        trackQueue = TrackQueue()
    }

    @Test
    fun `One Track - Peek`() {
        val track1 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)

        assertThat(trackQueue.peekActive(), nullValue())
        assertThat(trackQueue.peekNext(), equalTo(track1))
    }

    @Test
    fun `One Track - Poll Active from Inactive Queue`() {
        val track1 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)

        assertThat(trackQueue.pollActive(), equalTo(Pair(null, track1)))

        assertThat(trackQueue.peekActive(), equalTo(track1))
        assertThat(trackQueue.peekNext(), nullValue())
    }

    @Test
    fun `One Track - Poll Active from Activated Queue`() {
        val track1 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pollActive()

        assertThat(trackQueue.pollActive(), equalTo(Pair(track1, null)))

        assertThat(trackQueue.peekActive(), nullValue())
        assertThat(trackQueue.peekNext(), nullValue())
    }

    @Test
    fun `One Track - Poll Next from Inactive Queue`() {
        val track1 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)

        assertThat(trackQueue.pollNext(), equalTo(track1))

        assertThat(trackQueue.peekActive(), nullValue())
        assertThat(trackQueue.peekNext(), nullValue())
    }

    @Test
    fun `One Track - Poll Next from Activated Queue`() {
        val track1 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pollActive()

        assertThat(trackQueue.pollNext(), nullValue())

        assertThat(trackQueue.peekActive(), equalTo(track1))
        assertThat(trackQueue.peekNext(), nullValue())
    }

    @Test
    fun `Two Tracks - Peek`() {
        val track1 = TestUtil.randomFileVirtualTrack()
        val track2 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pushNext(track2)
        trackQueue.pollActive()

        assertThat(trackQueue.peekActive(), equalTo(track1))
        assertThat(trackQueue.peekNext(), equalTo(track2))
    }

    @Test
    fun `Two Tracks - Poll Active`() {
        val track1 = TestUtil.randomFileVirtualTrack()
        val track2 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pushNext(track2)
        trackQueue.pollActive()

        assertThat(trackQueue.pollActive(), equalTo(Pair(track1, track2)))

        assertThat(trackQueue.peekActive(), equalTo(track2))
        assertThat(trackQueue.peekNext(), nullValue())
    }

    @Test
    fun `Two Tracks - Poll Next`() {
        val track1 = TestUtil.randomFileVirtualTrack()
        val track2 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pushNext(track2)
        trackQueue.pollActive()

        assertThat(trackQueue.pollNext(), equalTo(track2))

        assertThat(trackQueue.peekActive(), equalTo(track1))
        assertThat(trackQueue.peekNext(), nullValue())
    }

    @Test
    fun `Three Tracks - Peek`() {
        val track1 = TestUtil.randomFileVirtualTrack()
        val track2 = TestUtil.randomFileVirtualTrack()
        val track3 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pushNext(track2)
        trackQueue.pushNext(track3)
        trackQueue.pollActive()

        assertThat(trackQueue.peekActive(), equalTo(track1))
        assertThat(trackQueue.peekNext(), equalTo(track2))
    }

    @Test
    fun `Three Tracks - Poll Active`() {
        val track1 = TestUtil.randomFileVirtualTrack()
        val track2 = TestUtil.randomFileVirtualTrack()
        val track3 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pushNext(track2)
        trackQueue.pushNext(track3)
        trackQueue.pollActive()

        assertThat(trackQueue.pollActive(), equalTo(Pair(track1, track2)))

        assertThat(trackQueue.peekActive(), equalTo(track2))
        assertThat(trackQueue.peekNext(), equalTo(track3))
    }

    @Test
    fun `Three Tracks - Poll Next`() {
        val track1 = TestUtil.randomFileVirtualTrack()
        val track2 = TestUtil.randomFileVirtualTrack()
        val track3 = TestUtil.randomFileVirtualTrack()

        trackQueue.pushNext(track1)
        trackQueue.pushNext(track2)
        trackQueue.pushNext(track3)
        trackQueue.pollActive()

        assertThat(trackQueue.pollNext(), equalTo(track2))

        assertThat(trackQueue.peekActive(), equalTo(track1))
        assertThat(trackQueue.peekNext(), nullValue())
    }

}