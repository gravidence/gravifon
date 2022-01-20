package org.gravidence.gravifon

import org.gravidence.gravifon.domain.FieldKeyExt
import org.gravidence.gravifon.domain.FieldValues
import org.gravidence.gravifon.domain.FileVirtualTrack

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

    fun randomFileVirtualTrack(): FileVirtualTrack {
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
                        randomAlphabeticString(8)
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

}