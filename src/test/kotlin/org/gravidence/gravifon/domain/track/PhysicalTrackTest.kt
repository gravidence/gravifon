package org.gravidence.gravifon.domain.track

import org.gravidence.gravifon.TestUtil
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PhysicalTrackTest {

    @Test
    fun tagsFromWav() {
        val physicalTrack = PhysicalTrack(TestUtil.resourceFromClasspath("/silence.wav"))
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(11, virtualTrack.fields.size)

        assertEquals("Silence Artist", virtualTrack.getArtist())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals("Silence Genre", virtualTrack.getGenre())
        assertEquals("Silence Comment", virtualTrack.getComment())
    }

    @Test
    fun tagsFromFlac() {
        val physicalTrack = PhysicalTrack(TestUtil.resourceFromClasspath("/silence.flac"))
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(12, virtualTrack.fields.size)

        assertEquals("Silence Artist", virtualTrack.getArtist())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals("Silence Genre", virtualTrack.getGenre())
        assertEquals("Silence Comment", virtualTrack.getComment())
    }

    @Test
    fun tagsFromOgg() {
        val physicalTrack = PhysicalTrack(TestUtil.resourceFromClasspath("/silence.ogg"))
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(12, virtualTrack.fields.size)

        assertEquals("Silence Artist", virtualTrack.getArtist())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals("Silence Genre", virtualTrack.getGenre())
        assertEquals("Silence Comment", virtualTrack.getComment())
    }

    @Test
    fun tagsFromMp3ID3v1() {
        val physicalTrack = PhysicalTrack(TestUtil.resourceFromClasspath("/silence_id3v1.mp3"))
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(7, virtualTrack.fields.size)

        assertEquals("Silence Artist", virtualTrack.getArtist())
        assertNull(virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        // ID3v1 supports only predefined set of genres
        assertEquals("Blues", virtualTrack.getGenre())
        assertEquals("Silence Comment", virtualTrack.getComment())
    }

    @Test
    fun tagsFromMp3ID3v2() {
        val physicalTrack = PhysicalTrack(TestUtil.resourceFromClasspath("/silence_id3v2.mp3"))
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(11, virtualTrack.fields.size)

        assertEquals("Silence Artist", virtualTrack.getArtist())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals("Silence Genre", virtualTrack.getGenre())
        assertEquals("Silence Comment", virtualTrack.getComment())
    }

}