package org.gravidence.gravifon.domain.track

import org.junit.jupiter.api.Test
import org.springframework.core.io.ClassPathResource
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class PhysicalTrackTest {

    @Test
    fun tagsFromWav() {
        val physicalTrack = PhysicalTrack(ClassPathResource("/silence.wav").uri)
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(11, virtualTrack.fields.size)

        assertEquals("Silence Artist 1, Silence Artist 2", virtualTrack.getArtist())
        assertEquals(setOf("Silence Artist 1", "Silence Artist 2"), virtualTrack.getArtists())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals(setOf("Silence Genre 1", "Silence Genre 2"), virtualTrack.getGenres())
        assertEquals("Silence Comment", virtualTrack.getComment())
        assertEquals("2", virtualTrack.getTrack())
        assertEquals("8", virtualTrack.getTrackTotal())
        assertEquals("1", virtualTrack.getDisc())
        assertEquals("2", virtualTrack.getDiscTotal())

        assertEquals("Silence Tag", virtualTrack.getCustomFieldValue("MY_TAG"))
    }

    @Test
    fun tagsFromFlac() {
        val physicalTrack = PhysicalTrack(ClassPathResource("/silence.flac").uri)
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(12, virtualTrack.fields.size)

        assertEquals("Silence Artist 1, Silence Artist 2", virtualTrack.getArtist())
        assertEquals(setOf("Silence Artist 1", "Silence Artist 2"), virtualTrack.getArtists())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals(setOf("Silence Genre 1", "Silence Genre 2"), virtualTrack.getGenres())
        assertEquals("Silence Comment", virtualTrack.getComment())
        assertEquals("2", virtualTrack.getTrack())
        assertEquals("8", virtualTrack.getTrackTotal())
        assertEquals("1", virtualTrack.getDisc())
        assertEquals("2", virtualTrack.getDiscTotal())

        assertEquals("Silence Tag", virtualTrack.getCustomFieldValue("MY_TAG"))
    }

    @Test
    fun tagsFromOgg() {
        val physicalTrack = PhysicalTrack(ClassPathResource("/silence.ogg").uri)
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(12, virtualTrack.fields.size)

        assertEquals("Silence Artist 1, Silence Artist 2", virtualTrack.getArtist())
        assertEquals(setOf("Silence Artist 1", "Silence Artist 2"), virtualTrack.getArtists())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals(setOf("Silence Genre 1", "Silence Genre 2"), virtualTrack.getGenres())
        assertEquals("Silence Comment", virtualTrack.getComment())
        assertEquals("2", virtualTrack.getTrack())
        assertEquals("8", virtualTrack.getTrackTotal())
        assertEquals("1", virtualTrack.getDisc())
        assertEquals("2", virtualTrack.getDiscTotal())

        assertEquals("Silence Tag", virtualTrack.getCustomFieldValue("MY_TAG"))
    }

    @Test
    fun tagsFromMp3ID3v1() {
        val physicalTrack = PhysicalTrack(ClassPathResource("/silence_id3v1.mp3").uri)
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
        assertEquals("2", virtualTrack.getTrack())
        assertNull(virtualTrack.getTrackTotal())
        assertNull(virtualTrack.getDisc())
        assertNull(virtualTrack.getDiscTotal())
    }

    @Test
    fun tagsFromMp3ID3v2() {
        val physicalTrack = PhysicalTrack(ClassPathResource("/silence_id3v2.mp3").uri)
        val virtualTrack = physicalTrack.toVirtualTrack()

        assertEquals(11, virtualTrack.fields.size)

        assertEquals("Silence Artist 1, Silence Artist 2", virtualTrack.getArtist())
        assertEquals(setOf("Silence Artist 1", "Silence Artist 2"), virtualTrack.getArtists())
        assertEquals("Silence Artists Collective", virtualTrack.getAlbumArtist())
        assertEquals("Silence Album", virtualTrack.getAlbum())
        assertEquals("2022-01-22", virtualTrack.getDate())
        assertEquals(2022, virtualTrack.getYear())
        assertEquals("Silence Track", virtualTrack.getTitle())
        assertEquals(setOf("Silence Genre 1", "Silence Genre 2"), virtualTrack.getGenres())
        assertEquals("Silence Comment", virtualTrack.getComment())
        assertEquals("2", virtualTrack.getTrack())
        assertEquals("8", virtualTrack.getTrackTotal())
        assertEquals("1", virtualTrack.getDisc())
        assertEquals("2", virtualTrack.getDiscTotal())

        assertEquals("Silence Tag", virtualTrack.getCustomFieldValue("MY_TAG"))
    }

}