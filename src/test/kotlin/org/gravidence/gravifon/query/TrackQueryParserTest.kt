package org.gravidence.gravifon.query

import kotlinx.datetime.Clock
import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.domain.track.FileVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import kotlin.test.assertEquals

internal class TrackQueryParserTest {

    lateinit var parser: TrackQueryParser

    @BeforeEach
    fun setUp() {
        parser = TrackQueryParser()
    }

    @Test
    fun validateClassProperty() {
        assertTrue(parser.validate("path eq '~/unittest.mp3'"))
    }

    @Test
    fun validateClassMethod() {
        assertTrue(parser.validate("date gt 1995"))
    }

    @Test
    fun validateUnknownReference() {
        assertFalse(parser.validate("notexistingproperty eq true"))
    }

    @Test
    fun validateInvalidQuery() {
        assertFalse(parser.validate("artist is 'Boards of Canada'")) // unsupported operation 'is'
        assertFalse(parser.validate("artist eq ('Boards of Canada'")) // not closed round bracket
        assertFalse(parser.validate("artist")) // query result is String
    }

    @Test
    fun executeClassProperty() {
        val fileVirtualTrack = TestUtil.randomFileVirtualTrack()
        assertTrue(parser.execute("path eq '${fileVirtualTrack.path}'", fileVirtualTrack))

        val virtualTrack = fileVirtualTrack as VirtualTrack
        assertTrue(parser.execute("path eq '${fileVirtualTrack.path}'", virtualTrack))
    }

    @Test
    fun executeClassMethod() {
        val fileVirtualTrack = TestUtil.randomFileVirtualTrack()
        val originalYear = fileVirtualTrack.getYear()
        assertTrue(parser.execute("year eq $originalYear", fileVirtualTrack))
        assertFalse(parser.execute("year lt $originalYear", fileVirtualTrack))
        assertTrue(parser.execute("year gt ${originalYear?.minus(10)}", fileVirtualTrack))

        val virtualTrack = fileVirtualTrack as VirtualTrack
        assertTrue(parser.execute("year eq $originalYear", virtualTrack))
        assertFalse(parser.execute("year lt $originalYear", virtualTrack))
        assertTrue(parser.execute("year gt ${originalYear?.minus(10)}", virtualTrack))
    }

    @Test
    fun executeMissingProperty() {
        val fileVirtualTrack = FileVirtualTrack(path = "music.mp3")
        assertTrue(parser.execute("album eq null", fileVirtualTrack))
        assertFalse(parser.execute("!(album eq null)", fileVirtualTrack))
    }

    @Test
    fun executeInvalidQuery() {
        val fileVirtualTrack = FileVirtualTrack(path = "music.mp3")
        assertFalse(parser.execute("path", fileVirtualTrack)) // query result is String
    }

    @Test
    fun executeFilterPlaylist() {
        val track1 = TestUtil.randomFileVirtualTrack(album = "alb1")
        val track2 = TestUtil.randomFileVirtualTrack(album = "alb2")
        val track3 = TestUtil.randomFileVirtualTrack(album = "alb1")
        val track4 = TestUtil.randomFileVirtualTrack(album = "alb2")
        val track5 = TestUtil.randomFileVirtualTrack(album = "alb1")

        val matchingTracks = parser.execute("album eq 'alb2'", listOf(track1, track2, track3, track4, track5))
        assertEquals(2, matchingTracks.size)
        assertTrue(matchingTracks.contains(track2))
        assertTrue(matchingTracks.contains(track4))
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun executeFilterBigCollection() {
        val numberOfTracks = 50000
        val tracks = TestUtil.manyRandomFileVirtualTracks(numberOfTracks)

        val start = Clock.System.now()
        parser.execute("year gt 2000 and artist >= 'x'", tracks)
        val finish = Clock.System.now()

        val duration = finish.minus(start).inWholeMilliseconds
        println("Query execution against $numberOfTracks tracks took ${duration}ms")
        assertTrue(duration < 500)
    }

}