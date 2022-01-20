package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class VirtualTrack : Track {

    abstract val fields: MutableMap<FieldKey, FieldValues>?

    override fun toString(): String {
        return uri().toString()
    }

    fun getFieldValues(key: FieldKeyExt): Set<String>? {
        return fields?.get(key.name)?.values
    }

    fun getFieldValue(key: FieldKeyExt): String? {
        return getFieldValues(key)?.firstOrNull()
    }

    fun setFieldValues(key: FieldKeyExt, values: FieldValues) {
        fields?.replace(key.name, values)
    }

    fun setFieldValues(key: FieldKeyExt, value: String) {
        setFieldValues(key, FieldValues(value))
    }

    fun clearField(key: FieldKeyExt) {
        fields?.remove(key.name)
    }

    fun getArtist(): String? {
        return getFieldValues(FieldKeyExt.ARTIST)?.joinToString(separator = ", ")
    }

    fun setArtist(value: String) {
        setFieldValues(FieldKeyExt.ARTIST, value)
    }

    fun getArtists(): Set<String>? {
        return getFieldValues(FieldKeyExt.ARTIST)
    }

    fun setArtists(values: MutableSet<String>) {
        setFieldValues(FieldKeyExt.ARTIST, FieldValues(values))
    }

    fun getTitle(): String? {
        return getFieldValue(FieldKeyExt.TITLE)
    }

    fun setTitle(value: String) {
        setFieldValues(FieldKeyExt.TITLE, value)
    }

    fun getAlbum(): String? {
        return getFieldValue(FieldKeyExt.ALBUM)
    }

    fun setAlbum(value: String) {
        setFieldValues(FieldKeyExt.ALBUM, value)
    }

    fun getYear(): Int? {
        return getFieldValue(FieldKeyExt.YEAR)?.toIntOrNull()
    }

    fun setYear(value: Int) {
        setFieldValues(FieldKeyExt.YEAR, value.toString())
    }

    fun getDate(): String? {
        return getFieldValue(FieldKeyExt.RECORDINGDATE)
    }

    fun setDate(value: String) {
        setFieldValues(FieldKeyExt.RECORDINGDATE, value)
    }

}