package org.gravidence.gravifon.plugin.bandcamp.model

import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.core.io.ClassPathResource
import java.nio.file.Files
import java.nio.file.Path

internal class BandcampItemTest {

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/album-single-artist.json",
            "/bandcamp/album-single-artist_album-title-has-artist.json",
            "/bandcamp/album-single-artist_track-title-has-no-artist.json",
        ]
    )
    fun enhanceAlbumSingleArtist(path: String) {
        val data = Files.readString(Path.of(ClassPathResource(path).uri))
        val actual = bandcampSerializer.decodeFromString<BandcampItem>(data)
            .enhanced()

        val expected = BandcampItem(
            url = "https://gravifon.org/album/qwe",
            type = BandcampItemType.ALBUM,
            details = BandcampItemDetails(
                artist = "QWE",
                title = "0101",
                date = Instant.parse("2020-02-20T00:00:00Z")
            ),

            albumArtist = "QWE",
            albumReleaseDate = Instant.parse("2020-02-20T00:00:00Z"),
            albumUrl = null,

            tracks = listOf(
                BandcampTrackDetails(
                    artist = "QWE",
                    title = "AAA",
                    tracknum = 1,
                    duration = 100.1,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/aaa"
                    )
                ),
                BandcampTrackDetails(
                    artist = "QWE",
                    title = "BBB",
                    tracknum = 2,
                    duration = 200.2,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/bbb"
                    )
                ),
            )
        )

        assertThat(expected.url, equalTo(actual.url))
        assertThat(expected.type, equalTo(actual.type))
        assertThat(expected.details, equalTo(actual.details))
        assertThat(expected.albumArtist, equalTo(actual.albumArtist))
        assertThat(expected.albumReleaseDate, equalTo(actual.albumReleaseDate))
        assertThat(expected.albumUrl, equalTo(actual.albumUrl))
        assertThat(expected.tracks, equalTo(actual.tracks))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/album-multi-artist.json",
            "/bandcamp/album-multi-artist_track-title-has-artist.json",
        ]
    )
    fun enhanceAlbumMultiArtist(path: String) {
        val data = Files.readString(Path.of(ClassPathResource(path).uri))
        val actual = bandcampSerializer.decodeFromString<BandcampItem>(data)
            .enhanced()

        val expected = BandcampItem(
            url = "https://gravifon.org/album/qwe",
            type = BandcampItemType.ALBUM,
            details = BandcampItemDetails(
                artist = "QWE",
                title = "0101",
                date = Instant.parse("2020-02-20T00:00:00Z")
            ),

            albumArtist = "QWE",
            albumReleaseDate = Instant.parse("2020-02-20T00:00:00Z"),
            albumUrl = null,

            tracks = listOf(
                BandcampTrackDetails(
                    artist = "ASD",
                    title = "AAA",
                    tracknum = 1,
                    duration = 100.1,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/aaa"
                    )
                ),
                BandcampTrackDetails(
                    artist = "ZXC",
                    title = "BBB",
                    tracknum = 2,
                    duration = 200.2,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/bbb"
                    )
                ),
            )
        )

        assertThat(expected.url, equalTo(actual.url))
        assertThat(expected.type, equalTo(actual.type))
        assertThat(expected.details, equalTo(actual.details))
        assertThat(expected.albumArtist, equalTo(actual.albumArtist))
        assertThat(expected.albumReleaseDate, equalTo(actual.albumReleaseDate))
        assertThat(expected.albumUrl, equalTo(actual.albumUrl))
        assertThat(expected.tracks, equalTo(actual.tracks))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/track-single-artist.json",
            "/bandcamp/track-single-artist_track-title-has-artist.json",
        ]
    )
    fun enhanceTrackSingleArtist(path: String) {
        val data = Files.readString(Path.of(ClassPathResource(path).uri))
        val actual = bandcampSerializer.decodeFromString<BandcampItem>(data)
            .enhanced()

        val expected = BandcampItem(
            url = "https://gravifon.org/track/aaa",
            type = BandcampItemType.TRACK,
            details = BandcampItemDetails(
                artist = "QWE",
                title = "AAA",
                date = Instant.parse("2020-01-20T00:00:00Z")
            ),

            albumArtist = "QWE",
            albumReleaseDate = Instant.parse("2020-01-20T00:00:00Z"),
            albumUrl = "/album/0101",

            tracks = listOf(
                BandcampTrackDetails(
                    artist = "QWE",
                    title = "AAA",
                    tracknum = 1,
                    duration = 100.1,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/aaa"
                    )
                ),
            )
        )

        assertThat(expected.url, equalTo(actual.url))
        assertThat(expected.type, equalTo(actual.type))
        assertThat(expected.details, equalTo(actual.details))
        assertThat(expected.albumArtist, equalTo(actual.albumArtist))
        assertThat(expected.albumReleaseDate, equalTo(actual.albumReleaseDate))
        assertThat(expected.albumUrl, equalTo(actual.albumUrl))
        assertThat(expected.tracks, equalTo(actual.tracks))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/track-multi-artist.json",
            "/bandcamp/track-multi-artist_track-title-has-artist.json",
        ]
    )
    fun enhanceTrackMultiArtist(path: String) {
        val data = Files.readString(Path.of(ClassPathResource(path).uri))
        val actual = bandcampSerializer.decodeFromString<BandcampItem>(data)
            .enhanced()

        val expected = BandcampItem(
            url = "https://gravifon.org/track/aaa",
            type = BandcampItemType.TRACK,
            details = BandcampItemDetails(
                artist = "ASD",
                title = "AAA",
                date = Instant.parse("2020-01-20T00:00:00Z")
            ),

            albumArtist = "QWE",
            albumReleaseDate = Instant.parse("2020-01-20T00:00:00Z"),
            albumUrl = "/album/0101",

            tracks = listOf(
                BandcampTrackDetails(
                    artist = "ASD",
                    title = "AAA",
                    tracknum = 1,
                    duration = 100.1,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/aaa"
                    )
                ),
            )
        )

        assertThat(expected.url, equalTo(actual.url))
        assertThat(expected.type, equalTo(actual.type))
        assertThat(expected.details, equalTo(actual.details))
        assertThat(expected.albumArtist, equalTo(actual.albumArtist))
        assertThat(expected.albumReleaseDate, equalTo(actual.albumReleaseDate))
        assertThat(expected.albumUrl, equalTo(actual.albumUrl))
        assertThat(expected.tracks, equalTo(actual.tracks))
    }

}