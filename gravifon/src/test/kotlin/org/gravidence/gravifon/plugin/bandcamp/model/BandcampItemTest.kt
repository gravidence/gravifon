package org.gravidence.gravifon.plugin.bandcamp.model

import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test
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
            "/bandcamp/album-single-artist_track-has-no-artist.json",
            "/bandcamp/album-single-artist_root-has-no-album-release-date.json",
            "/bandcamp/album-single-artist_album-has-no-release-date.json",
            "/bandcamp/album-single-artist_album-title-has-non-default-separator.json",
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

        assertThat(actual.url, equalTo(expected.url))
        assertThat(actual.type, equalTo(expected.type))
        assertThat(actual.details, equalTo(expected.details))
        assertThat(actual.albumArtist, equalTo(expected.albumArtist))
        assertThat(actual.albumReleaseDate, equalTo(expected.albumReleaseDate))
        assertThat(actual.albumUrl, equalTo(expected.albumUrl))
        assertThat(actual.tracks, equalTo(expected.tracks))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/album-single-artist_some-track-title-has-known-separator.json",
        ]
    )
    fun enhanceAlbumSingleArtistCornerCases(path: String) {
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
                    title = "AAA CCC",
                    tracknum = 1,
                    duration = 100.1,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/aaa"
                    )
                ),
                BandcampTrackDetails(
                    artist = "QWE",
                    title = "BBB . DDD",
                    tracknum = 2,
                    duration = 200.2,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/bbb"
                    )
                ),
                BandcampTrackDetails(
                    artist = "QWE",
                    title = "EEE",
                    tracknum = 3,
                    duration = 300.3,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/eee"
                    )
                ),
                BandcampTrackDetails(
                    artist = "QWE",
                    title = "FFF",
                    tracknum = 4,
                    duration = 400.4,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/fff"
                    )
                ),
            )
        )

        assertThat(actual.url, equalTo(expected.url))
        assertThat(actual.type, equalTo(expected.type))
        assertThat(actual.details, equalTo(expected.details))
        assertThat(actual.albumArtist, equalTo(expected.albumArtist))
        assertThat(actual.albumReleaseDate, equalTo(expected.albumReleaseDate))
        assertThat(actual.albumUrl, equalTo(expected.albumUrl))
        assertThat(actual.tracks, equalTo(expected.tracks))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/album-multi-artist.json",
            "/bandcamp/album-multi-artist_track-title-has-artist.json",
            "/bandcamp/album-multi-artist_track-title-has-different-artists.json",
            "/bandcamp/album-multi-artist_unnecessary-spaces.json",
            "/bandcamp/album-multi-artist_track-title-has-non-default-separator.json",
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
                artist = "QWE QWE",
                title = "01 01",
                date = Instant.parse("2020-02-20T00:00:00Z")
            ),

            albumArtist = "QWE QWE",
            albumReleaseDate = Instant.parse("2020-02-20T00:00:00Z"),
            albumUrl = null,

            tracks = listOf(
                BandcampTrackDetails(
                    artist = "ASD ASD",
                    title = "AAA AAA",
                    tracknum = 1,
                    duration = 100.1,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/aaa"
                    )
                ),
                BandcampTrackDetails(
                    artist = "ZXC ZXC",
                    title = "BBB BBB",
                    tracknum = 2,
                    duration = 200.2,
                    file = BandcampTrackFileInfo(
                        mp3128 = "https://gravifon.org/stream/bbb"
                    )
                ),
            )
        )

        assertThat(actual.url, equalTo(expected.url))
        assertThat(actual.type, equalTo(expected.type))
        assertThat(actual.details, equalTo(expected.details))
        assertThat(actual.albumArtist, equalTo(expected.albumArtist))
        assertThat(actual.albumReleaseDate, equalTo(expected.albumReleaseDate))
        assertThat(actual.albumUrl, equalTo(expected.albumUrl))
        assertThat(actual.tracks, equalTo(expected.tracks))
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "/bandcamp/track-single-artist.json",
            "/bandcamp/track-single-artist_track-has-no-artist.json",
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

        assertThat(actual.url, equalTo(expected.url))
        assertThat(actual.type, equalTo(expected.type))
        assertThat(actual.details, equalTo(expected.details))
        assertThat(actual.albumArtist, equalTo(expected.albumArtist))
        assertThat(actual.albumReleaseDate, equalTo(expected.albumReleaseDate))
        assertThat(actual.albumUrl, equalTo(expected.albumUrl))
        assertThat(actual.tracks, equalTo(expected.tracks))
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

        assertThat(actual.url, equalTo(expected.url))
        assertThat(actual.type, equalTo(expected.type))
        assertThat(actual.details, equalTo(expected.details))
        assertThat(actual.albumArtist, equalTo(expected.albumArtist))
        assertThat(actual.albumReleaseDate, equalTo(expected.albumReleaseDate))
        assertThat(actual.albumUrl, equalTo(expected.albumUrl))
        assertThat(actual.tracks, equalTo(expected.tracks))
    }

    @Test
    fun streamExpiresAfter() {
        val fileInfo = BandcampTrackFileInfo(
            mp3128 = "https://gravifon.org/stream/1234567890?p=0&ts=1646427552&t=1e6feffdaf15c0039b25d2eb99ab18c50d1c2b63&token=1646427552_5119aec272539e7d3809dd2d8e0e4756512e988d"
        )

        assertThat(fileInfo.expiresAfter(), equalTo(Instant.fromEpochSeconds(1646427552)))
    }

}