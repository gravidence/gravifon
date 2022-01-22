package org.gravidence.gravifon.domain.track

import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.tag.FieldKey
import java.net.URI

val dateRegex = """^(\d{4})(-\d{2}(-\d{2})?)?""".toRegex()

@Serializable
sealed class VirtualTrack {

    abstract val fields: MutableMap<FieldKey, FieldValues>

    abstract fun uri(): URI

    override fun toString(): String {
        return uri().toString()
    }

    fun getFieldValues(key: FieldKey): Set<String>? {
        return fields[key]?.values
    }

    fun getFieldValue(key: FieldKey): String? {
        return getFieldValues(key)?.firstOrNull()
    }

    fun setFieldValues(key: FieldKey, values: FieldValues) {
        fields[key] = values
    }

    fun setFieldValues(key: FieldKey, value: String?) {
        if (value == null) {
            clearField(key)
        } else {
            setFieldValues(key, FieldValues(value))
        }
    }

    fun clearField(key: FieldKey) {
        fields.remove(key)
    }

    fun getArtist(): String? {
        return getFieldValues(FieldKey.ARTIST)?.joinToString(separator = ", ")
    }

    fun setArtist(value: String?) {
        setFieldValues(FieldKey.ARTIST, value)
    }

    fun getArtists(): Set<String>? {
        return getFieldValues(FieldKey.ARTIST)
    }

    fun setArtists(values: MutableSet<String>?) {
        if (values == null) {
            setArtist(null)
        } else {
            setFieldValues(FieldKey.ARTIST, FieldValues(values))
        }
    }

    fun getAlbumArtist(): String? {
        return getFieldValues(FieldKey.ALBUM_ARTIST)?.joinToString(separator = ", ")
    }

    fun setAlbumArtist(value: String?) {
        setFieldValues(FieldKey.ALBUM_ARTIST, value)
    }

    fun getTitle(): String? {
        return getFieldValue(FieldKey.TITLE)
    }

    fun setTitle(value: String?) {
        setFieldValues(FieldKey.TITLE, value)
    }

    fun getAlbum(): String? {
        return getFieldValue(FieldKey.ALBUM)
    }

    fun setAlbum(value: String?) {
        setFieldValues(FieldKey.ALBUM, value)
    }

    fun getDate(): String? {
        return getFieldValue(FieldKey.YEAR)
    }

    fun setDate(value: String?) {
        setFieldValues(FieldKey.YEAR, value)
    }

    fun getYear(): Int? {
        return getDate()?.let {
            dateRegex.matchEntire(it)?.groupValues?.get(1)?.toIntOrNull()
        }
    }

    fun getGenre(): String? {
        return getFieldValue(FieldKey.GENRE)
    }

    fun setGenre(value: String?) {
        setFieldValues(FieldKey.GENRE, value)
    }

    fun getComment(): String? {
        return getFieldValue(FieldKey.COMMENT)
    }

    fun setComment(value: String?) {
        setFieldValues(FieldKey.COMMENT, value)
    }

}