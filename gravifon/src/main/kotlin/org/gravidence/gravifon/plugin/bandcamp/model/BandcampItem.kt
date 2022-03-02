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

@Serializable
data class BandcampItem(
    val url: String,
    @SerialName("item_type")
    val type: BandcampItemType,
    @SerialName("current")
    val details: BandcampItemDetails,

    @SerialName("artist")
    val albumArtist: String,
    @SerialName("album_release_date")
    @Serializable(with = InstantAsRFC1123StringSerializer::class)
    val albumReleaseDate: Instant,
    @SerialName("album_url")
    val albumUrl: String? = null,

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
    val artist: String? = null,
    val title: String? = null,
    @SerialName("release_date")
    @Serializable(with = InstantAsRFC1123StringSerializer::class)
    val date: Instant? = null,
)

@Serializable
data class BandcampTrackDetails(
    val artist: String? = null,
    val title: String,
    @SerialName("track_num")
    val tracknum: Int,
    val duration: Double,
    val file: BandcampTrackFileInfo,
)

@Serializable
data class BandcampTrackFileInfo(
    @SerialName("mp3-128")
    val mp3128: String,
)

private const val BC_SEPARATOR = " - "

fun BandcampItem.enhanced(): BandcampItem {
    return when (type) {
        BandcampItemType.ALBUM -> {
            val enhancedAlbumArtist = details.artist ?: albumArtist

            val isMultiArtist = tracks.all { it.artist == null && it.title.contains(BC_SEPARATOR) }

            copy(
                details = details.copy(
                    artist = enhancedAlbumArtist,
                    title = enhanceTitle(details.title!!, enhancedAlbumArtist),
                    date = details.date ?: albumReleaseDate
                ),
                tracks = tracks.map {
                    val enhancedArtist = it.artist ?: enhanceTrackArtist(it.title, isMultiArtist) ?: enhancedAlbumArtist
                    val enhancedTitle = enhanceTitle(it.title, enhancedArtist)

                    it.copy(artist = enhancedArtist, title = enhancedTitle)
                }
            )
        }
        BandcampItemType.TRACK -> {
            copy(
                details = details.copy(
                    date = details.date ?: albumReleaseDate
                ),
                tracks = tracks.map {
                    val enhancedArtist = details.artist ?: it.artist
                    val enhancedTitle = details.title ?: it.title

                    it.copy(artist = enhancedArtist, title = enhancedTitle)
                }
            )
        }
    }
}

private fun enhanceTrackArtist(title: String, isMultiArtist: Boolean): String? {
    return if (isMultiArtist) {
        title.substringBefore(BC_SEPARATOR)
    } else {
        null
    }
}
private fun enhanceTitle(title: String, artist: String): String {
    return title.removePrefix(artist + BC_SEPARATOR)
}