package org.gravidence.gravifon.query

import org.gravidence.gravifon.TestUtil
import org.gravidence.gravifon.domain.track.FileVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertTimeoutPreemptively
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import java.time.Duration
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
        val fileVirtualTrack = TestUtil.fixedFileVirtualTrack(date = "2014-07-19")
        assertTrue(parser.execute("year eq 2014", fileVirtualTrack))
        assertFalse(parser.execute("year lt 2014", fileVirtualTrack))
        assertTrue(parser.execute("year gt 2010", fileVirtualTrack))

        val virtualTrack = fileVirtualTrack as VirtualTrack
        assertTrue(parser.execute("year eq 2014", virtualTrack))
        assertFalse(parser.execute("year lt 2014", virtualTrack))
        assertTrue(parser.execute("year gt 2010", virtualTrack))
    }

    @Test
    fun executeMissingProperty() {
        val fileVirtualTrack = FileVirtualTrack(path = "music.mp3")
        assertTrue(parser.execute("album eq null", fileVirtualTrack))
        assertFalse(parser.execute("!(album eq null)", fileVirtualTrack))
    }

    @Test
    fun executeInvalidQuery() {
        assertFalse(parser.execute("path", FileVirtualTrack(path = "music.mp3"))) // query result is String
    }

    @Test
    @Disabled
    fun executeCaseSensitive() {
        assertTrue(parser.execute("title eq 'track 1'", TestUtil.fixedFileVirtualTrack(title = "Track 1")))
    }

    @Test
    @Disabled
    fun executeExtendedCharacterSet() {
        assertTrue(parser.execute("title eq 'Larmpegel'", TestUtil.fixedFileVirtualTrack(title = "Lärmpegel")))
        assertTrue(parser.execute("title eq 'Een'", TestUtil.fixedFileVirtualTrack(title = "Één")))
    }

    @Test
    fun executeFilterPlaylist() {
        val track1 = TestUtil.fixedFileVirtualTrack(album = "alb1")
        val track2 = TestUtil.fixedFileVirtualTrack(album = "alb2")
        val track3 = TestUtil.fixedFileVirtualTrack(album = "alb1")
        val track4 = TestUtil.fixedFileVirtualTrack(album = "alb2")
        val track5 = TestUtil.fixedFileVirtualTrack(album = "alb1")

        val matchingTracks = parser.execute("album eq 'alb2'", listOf(track1, track2, track3, track4, track5))
        assertEquals(2, matchingTracks.size)
        assertTrue(matchingTracks.contains(track2))
        assertTrue(matchingTracks.contains(track4))
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun executeFilterBigCollection() {
        val tracks = TestUtil.manyRandomFileVirtualTracks(50000)

        assertTimeoutPreemptively(Duration.ofMillis(250)) {
            parser.execute("year gt 2000 and artist >= 'x'", tracks)
        }
    }

}