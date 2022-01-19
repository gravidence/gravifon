package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class VirtualTrack(
) : Track {

    abstract val fields: MutableMap<FieldKey, FieldValues>?

    fun extractArtist(): String? {
        return fields?.get(FieldKeyExt.ARTIST.name)?.values?.joinToString(separator = ", ")
    }

    fun extractArtists(): List<String>? {
        return fields?.get(FieldKeyExt.ARTIST.name)?.values?.toList()
    }

    fun extractTitle(): String? {
        return fields?.get(FieldKeyExt.TITLE.name)?.values?.firstOrNull()
    }

    fun extractAlbum(): String? {
        return fields?.get(FieldKeyExt.ALBUM.name)?.values?.firstOrNull()
    }

    fun extractYear(): String? {
        return fields?.get(FieldKeyExt.YEAR.name)?.values?.firstOrNull()
    }

}