package org.gravidence.gravifon.domain.track

import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.tag.FieldValues
import org.gravidence.gravifon.domain.track.compare.VirtualTrackSelectors
import org.jaudiotagger.tag.FieldKey
import java.net.URI
import kotlin.time.Duration

val dateRegex = """^(\d{4})(-\d{2}(-\d{2})?)?""".toRegex()

/**
 * Builds a [VirtualTrack] comparator based on sequence of [selectors].
 */
fun virtualTrackComparator(selectors: List<VirtualTrackSelectors> = listOf(VirtualTrackSelectors.URI)): Comparator<VirtualTrack> {
    var comparator = compareBy((selectors.firstOrNull() ?: VirtualTrackSelectors.URI).selector)
    selectors.drop(1).forEach {
        comparator = comparator.thenBy(it.selector)
    }
    return comparator
}

@Serializable
sealed class VirtualTrack {

    abstract val headers: Headers
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

    fun getLength(): Duration? {
        return headers.length
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

    fun getGenres(): Set<String>? {
        return getFieldValues(FieldKey.GENRE)
    }

    fun getComment(): String? {
        return getFieldValue(FieldKey.COMMENT)
    }

    fun setComment(value: String?) {
        setFieldValues(FieldKey.COMMENT, value)
    }

    fun getTrack(): String? {
        return getFieldValue(FieldKey.TRACK)
    }

    fun setTrack(value: String?) {
        setFieldValues(FieldKey.TRACK, value)
    }

    fun getTrackTotal(): String? {
        return getFieldValue(FieldKey.TRACK_TOTAL)
    }

    fun setTrackTotal(value: String?) {
        setFieldValues(FieldKey.TRACK_TOTAL, value)
    }

    fun getDisc(): String? {
        return getFieldValue(FieldKey.DISC_NO)
    }

    fun setDisc(value: String?) {
        setFieldValues(FieldKey.DISC_NO, value)
    }

    fun getDiscTotal(): String? {
        return getFieldValue(FieldKey.DISC_TOTAL)
    }

    fun setDiscTotal(value: String?) {
        setFieldValues(FieldKey.DISC_TOTAL, value)
    }

}