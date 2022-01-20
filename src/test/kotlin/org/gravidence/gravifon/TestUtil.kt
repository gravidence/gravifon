package org.gravidence.gravifon

import org.gravidence.gravifon.domain.album.VirtualAlbum
import org.gravidence.gravifon.domain.tag.FieldKeyExt
import org.gravidence.gravifon.domain.tag.FieldValues
import org.gravidence.gravifon.domain.track.FileVirtualTrack

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

    fun randomFileVirtualTrack(album: String? = null): FileVirtualTrack {
        return FileVirtualTrack(
            path = randomAlphanumericString(20),
            fields = mutableMapOf(
                Pair(
                    FieldKeyExt.ARTIST.name, FieldValues(
                        randomAlphabeticString(8)
                    )
                ),
                Pair(
                    FieldKeyExt.TITLE.name, FieldValues(
                        randomAlphabeticString(8)
                    )
                ),
                Pair(
                    FieldKeyExt.ALBUM.name, FieldValues(
                        album ?: randomAlphabeticString(8)
                    )
                ),
                Pair(
                    FieldKeyExt.YEAR.name, FieldValues(
                        (1980..2020).random().toString()
                    )
                ),
                Pair(
                    FieldKeyExt.COMMENT.name, FieldValues(
                        randomAlphabeticString(8)
                    )
                )
            )
        )
    }

    fun manyRandomFileVirtualTracks(numberOfTracks: Int): List<FileVirtualTrack> {
        return List(numberOfTracks) { randomFileVirtualTrack() }
    }

    fun randomVirtualAlbum(): VirtualAlbum {
        val album = randomAlphabeticString(12)
        val numberOfTracks = (3..20).random()

        val firstTrack = randomFileVirtualTrack(album)
        val tracks = mutableListOf(firstTrack)
        for (i in 2..numberOfTracks) {
            tracks += randomFileVirtualTrack(album)
        }

        return VirtualAlbum(album, tracks.toMutableList())
    }

    fun manyRandomVirtualAlbums(numberOfAlbums: Int): List<VirtualAlbum> {
        return List(numberOfAlbums) { randomVirtualAlbum() }
    }

}