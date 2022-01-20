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
    @EnabledIfEnvironmentVariable(named = "gravifon.test.performance", matches = "enable")
    fun executeBigCollection() {
        val numberOfTracks = 50000
        val tracks = TestUtil.manyRandomFileVirtualTracks(numberOfTracks)

        val start = Clock.System.now()
        tracks.filter { parser.execute("year gt 2000 and artist >= 'x'", it) }
        val finish = Clock.System.now()

        val duration = finish.minus(start).inWholeMilliseconds
        assertTrue(duration < 500)
    }

}