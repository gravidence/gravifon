package org.gravidence.gravifon.plugin.bandcamp.model

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.http4k.core.Uri
import org.http4k.core.queries

@Serializable
data class BandcampItem(
    @SerialName("url")
    val url: String,
    @SerialName("item_type")
    val type: BandcampItemType,
    @SerialName("current")
    val details: BandcampItemDetails,

    @SerialName("artist")
    val albumArtist: String,
    @SerialName("album_release_date")
    @Serializable(with = InstantAsRFC1123StringSerializer::class)
    val albumReleaseDate: Instant?,
    @SerialName("album_url")
    val albumUrl: String? = null,

    @SerialName("play_cap_data")
    val playCapDetails: BandcampPlayCapDetails? = null,
    @SerialName("trackinfo")
    val tracks: List<BandcampTrackDetails>,
) {

    fun resolveAlbum(): String {
        return when (type) {
            BandcampItemType.ALBUM -> details.title ?: "" // TODO log a warning if no album title present?
            BandcampItemType.TRACK -> "" // TODO query actual album info, see url/albumUrl
        }
    }

}

@Serializable(with = BandcampItemType.AsStringSerializer::class)
enum class BandcampItemType(val code: String) {

    ALBUM("album"),
    TRACK("track");

    companion object {
        fun valueOfCode(code: String): BandcampItemType {
            return values().first { it.code == code }
        }
    }

    object AsStringSerializer : KSerializer<BandcampItemType> {

        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BandcampItemType", PrimitiveKind.STRING)

        override fun deserialize(decoder: Decoder): BandcampItemType {
            return valueOfCode(decoder.decodeString())
        }

        override fun serialize(encoder: Encoder, value: BandcampItemType) {
            encoder.encodeString(value.code)
        }

    }

}

@Serializable
data class BandcampItemDetails(
    @SerialName("artist")
    val artist: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("release_date")
    @Serializable(with = InstantAsRFC1123StringSerializer::class)
    val date: Instant? = null,
)

@Serializable
data class BandcampPlayCapDetails(
    @SerialName("streaming_limits_enabled")
    val enabled: Boolean,
    @SerialName("streaming_limit")
    val limit: Int,
)

@Serializable
data class BandcampTrackDetails(
    @SerialName("artist")
    val artist: String? = null,
    @SerialName("title")
    val title: String,
    @SerialName("track_num")
    val tracknum: Int?,
    @SerialName("duration")
    val duration: Double,
    @SerialName("is_capped")
    val isCapped: Boolean? = null,
    @SerialName("play_count")
    val playcount: Int? = null,
    @SerialName("file")
    val file: BandcampTrackFileInfo?,
)

@Serializable
data class BandcampTrackFileInfo(
    @SerialName("mp3-128")
    val mp3128: String,
)

fun BandcampTrackFileInfo.expiresAfter(): Instant? {
    return Uri.of(mp3128)
        .queries()
        .filter { it.first == "ts" }
        .map { it.second?.toLongOrNull() }
        .first()?.let { ts ->
            Instant.fromEpochSeconds(ts)
        }
}

private val duplicateWhitespaceRegex = """\s+""".toRegex()

fun BandcampItem.enhanced(): BandcampItem {
    return normalizeWhitespaces().run {
        val enhancedAlbumArtist = details.artist ?: albumArtist
        val enhancedAlbumReleaseDate = details.date ?: albumReleaseDate

        when (type) {
            BandcampItemType.ALBUM -> {
                val isMultiArtist = tracks.all { it.artist == null && DirtyTitle.regex.matches(it.title) }

                copy(
                    albumReleaseDate = enhancedAlbumReleaseDate,
                    details = details.copy(
                        artist = enhancedAlbumArtist,
                        title = DirtyTitle(details.title!!).getEnhancedTitle(enhancedAlbumArtist),
                        date = enhancedAlbumReleaseDate
                    ),
                    tracks = tracks.map { track ->
                        val dirtyTitle = DirtyTitle(track.title)

                        val enhancedArtist = track.artist
                            ?: dirtyTitle.getEnhancedArtist()?.takeIf { isMultiArtist }
                            ?: enhancedAlbumArtist
                        val enhancedTitle = dirtyTitle.getEnhancedTitle(enhancedArtist)

                        track.copy(artist = enhancedArtist, title = enhancedTitle)
                    }
                )
            }
            BandcampItemType.TRACK -> {
                copy(
                    albumReleaseDate = enhancedAlbumReleaseDate,
                    details = details.copy(
                        artist = enhancedAlbumArtist,
                        date = enhancedAlbumReleaseDate
                    ),
                    tracks = tracks.map {
                        val enhancedArtist = it.artist ?: enhancedAlbumArtist
                        val enhancedTitle = details.title ?: it.title

                        it.copy(artist = enhancedArtist, title = enhancedTitle)
                    }
                )
            }
        }
    }
}

fun BandcampItem.normalizeWhitespaces(): BandcampItem {
    return copy(
        albumArtist = albumArtist.normalizeWhitespaces(),
        details = details.copy(
            artist = details.artist?.normalizeWhitespaces(),
            title = details.title?.normalizeWhitespaces(),
        ),
        tracks = tracks.map {
            it.copy(
                artist = it.artist?.normalizeWhitespaces(),
                title = it.title.normalizeWhitespaces(),
            )
        },
    )
}

private class DirtyTitle(
    val originalTitle: String,
) {

    private val enhancedArtist: String?
    private val separator: String?
    private val enhancedTitle: String?

    init {
        val groups = regex.matchEntire(originalTitle)?.groups

        enhancedArtist = groups?.get(1)?.value
        separator = groups?.get(2)?.value
        enhancedTitle = groups?.get(3)?.value
    }

    fun getEnhancedArtist(): String? {
        return enhancedArtist
    }

    fun getEnhancedTitle(artist: String): String {
        return enhancedTitle?.takeIf { artist.equals(enhancedArtist, true) }
            ?: originalTitle
    }

    companion object {
        val regex = """(.+)(\s[-_:.]\s)(.+)""".toRegex()
    }

}

private fun String.normalizeWhitespaces(): String {
    return trim().replace(duplicateWhitespaceRegex, " ")
}