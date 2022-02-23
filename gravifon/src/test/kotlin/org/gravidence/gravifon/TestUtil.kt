package org.gravidence.gravifon

import org.gravidence.gravifon.domain.album.AlbumScanner
import org.gravidence.gravifon.domain.album.VirtualAlbum
import org.gravidence.gravifon.domain.track.FileVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack

object TestUtil {

    fun randomString(length: Int, charset: List<Char>): String {
        return List(length) { charset.random() }
            .joinToString("")
    }

    fun randomAlphabeticString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z')

        return randomString(length, charset)
    }

    fun randomAlphanumericString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')

        return randomString(length, charset)
    }

    fun fixedFileVirtualTrack(
        path: String = randomAlphanumericString(20),
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        albumArtist: String? = null,
        date: String? = null,
        comment: String? = null,
        genre: String? = null,
        track: String? = null,
        trackTotal: String? = null,
        disc: String? = null,
        discTotal: String? = null,
    ): FileVirtualTrack {
        return FileVirtualTrack(path = path).apply {
            setTitle(title)
            setArtist(artist)
            setAlbum(album)
            setAlbumArtist(albumArtist)
            setDate(date)
            setComment(comment)
            setGenre(genre)
            setTrack(track)
            setTrackTotal(trackTotal)
            setDisc(disc)
            setDiscTotal(discTotal)
        }
    }

    fun randomFileVirtualTrack(
        artist: String = randomAlphabeticString(8),
        album: String = randomAlphabeticString(12),
        albumArtist: String = randomAlphabeticString(8)
    ): FileVirtualTrack {
        return fixedFileVirtualTrack(
            title = randomAlphabeticString(10),
            artist = artist,
            album = album,
            albumArtist = albumArtist,
            date = (1980..2020).random().toString(),
            comment = randomAlphabeticString(30),
            genre = randomAlphabeticString(6)
        )
    }

    fun manyRandomFileVirtualTracks(numberOfTracks: Int): List<FileVirtualTrack> {
        return List(numberOfTracks) { randomFileVirtualTrack() }
    }

    fun randomVirtualAlbum(
        album: String = randomAlphabeticString(12),
        albumArtist: String = randomAlphabeticString(8),
        numberOfTracks: Int = (3..20).random()
    ): VirtualAlbum {
        val albumResolved = album
        val albumArtistResolved = albumArtist
        val numberOfTracksResolved = numberOfTracks

        val firstTrack = randomFileVirtualTrack(album = albumResolved, albumArtist = albumArtistResolved)
        val tracks = mutableListOf(firstTrack)
        for (i in 2..numberOfTracksResolved) {
            tracks += randomFileVirtualTrack(album = albumResolved, albumArtist = albumArtistResolved)
        }

        return virtualAlbumFromVirtualTracks(tracks)
    }

    fun manyRandomVirtualAlbums(numberOfAlbums: Int): List<VirtualAlbum> {
        return List(numberOfAlbums) { randomVirtualAlbum() }
    }

    /**
     * Creates a [VirtualAlbum] from [tracks].
     * No validation takes place: at least one track is expected,
     * as well as assumption is that all tracks have the same album key.
     */
    fun virtualAlbumFromVirtualTracks(tracks: List<VirtualTrack>): VirtualAlbum {
        return VirtualAlbum(AlbumScanner.calculateAlbumKey(tracks.first()), tracks.toMutableList())
    }

}