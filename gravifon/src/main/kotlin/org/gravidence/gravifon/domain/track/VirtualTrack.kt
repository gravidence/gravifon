package org.gravidence.gravifon.domain.track

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.tag.FieldValue
import org.gravidence.gravifon.domain.tag.FieldValues
import org.gravidence.gravifon.domain.track.compare.VirtualTrackSelectors
import org.gravidence.gravifon.util.DurationUtil
import org.gravidence.gravifon.util.serialization.gravifonSerializer
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
    abstract val customFields: MutableMap<String, FieldValues>

    abstract var failing: Boolean

    abstract fun uri(): URI

    // TODO assess if needed really
    fun clone(): VirtualTrack {
        return gravifonSerializer.decodeFromString(gravifonSerializer.encodeToString(this))
    }

    override fun toString(): String {
        return uri().toString()
    }

    fun getFieldValues(key: FieldKey): Set<FieldValue>? {
        return fields[key]?.values
    }

    fun getFieldValue(key: FieldKey): FieldValue? {
        return getFieldValues(key)?.firstOrNull()
    }

    fun setFieldValues(key: FieldKey, values: FieldValues) {
        fields[key] = values
    }

    fun setFieldValues(key: FieldKey, value: FieldValue?) {
        if (value == null) {
            clearField(key)
        } else {
            setFieldValues(key, FieldValues(value))
        }
    }

    fun clearField(key: FieldKey) {
        fields.remove(key)
    }

    fun getCustomFieldValues(key: String): Set<FieldValue>? {
        return customFields.get(key)?.values
    }

    fun getCustomFieldValue(key: String): FieldValue? {
        return getCustomFieldValues(key)?.firstOrNull()
    }

    fun setCustomFieldValues(key: String, values: FieldValues) {
        customFields.set(key, values)
    }

    fun setCustomFieldValues(key: String, value: FieldValue?) {
        if (value == null) {
            clearCustomField(key)
        } else {
            setCustomFieldValues(key, FieldValues(value))
        }
    }

    fun clearCustomField(key: String) {
        customFields.remove(key)
    }

    fun setFieldValues(key: String, values: FieldValues) {
        try {
            setFieldValues(FieldKey.valueOf(key), values)
        } catch (e: IllegalArgumentException) {
            setCustomFieldValues(key, values)
        }
    }

    fun setFieldValues(key: String, value: FieldValue?) {
        if (value == null) {
            clearField(key)
        } else {
            setFieldValues(key, FieldValues(value))
        }
    }

    fun clearField(key: String) {
        try {
            clearField(FieldKey.valueOf(key))
        } catch (e: IllegalArgumentException) {
            clearCustomField(key)
        }
    }

    fun getAllFields(): Map<String, FieldValues> {
        val map = mutableMapOf<String, FieldValues>()

        fields.forEach { map[it.key.name] = it.value }
        customFields.forEach { map[it.key] = it.value }

        return map
    }

    fun getLength(): Duration? {
        return headers.length
    }

    fun getLengthFormatShortHours(): String {
        return DurationUtil.formatShortHours(headers.length)
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