package org.gravidence.gravifon

import org.gravidence.gravifon.domain.album.AlbumScanner
import org.gravidence.gravifon.domain.album.VirtualAlbum
import org.gravidence.gravifon.domain.track.FileVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import java.net.URI

object TestUtil {

    fun resourceFromClasspath(path: String): URI {
        return this::class.java.getResource(path)?.toURI()!!
    }

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
        title: String? = null,
        artist: String? = null,
        album: String? = null,
        albumArtist: String? = null,
        date: String? = null,
        comment: String? = null,
        genre: String? = null,
    ): FileVirtualTrack {
        return FileVirtualTrack(path = randomAlphanumericString(20)).apply {
            setTitle(title)
            setArtist(artist)
            setAlbum(album)
            setAlbumArtist(albumArtist)
            setDate(date)
            setComment(comment)
            setGenre(genre)
        }
    }

    fun randomFileVirtualTrack(
        artist: String? = null,
        album: String? = null,
        albumArtist: String? = null
    ): FileVirtualTrack {
        return fixedFileVirtualTrack(
            title = randomAlphabeticString(10),
            artist = artist ?: randomAlphabeticString(8),
            album = album ?: randomAlphabeticString(12),
            albumArtist = albumArtist ?: randomAlphabeticString(8),
            date = (1980..2020).random().toString(),
            comment = randomAlphabeticString(30),
            genre = randomAlphabeticString(6)
        )
    }

    fun manyRandomFileVirtualTracks(numberOfTracks: Int): List<FileVirtualTrack> {
        return List(numberOfTracks) { randomFileVirtualTrack() }
    }

    fun randomVirtualAlbum(
        album: String? = null,
        albumArtist: String? = null,
        numberOfTracks: Int? = null
    ): VirtualAlbum {
        val albumResolved = album ?: randomAlphabeticString(12)
        val albumArtistResolved = albumArtist ?: randomAlphabeticString(8)
        val numberOfTracksResolved = numberOfTracks ?: (3..20).random()

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