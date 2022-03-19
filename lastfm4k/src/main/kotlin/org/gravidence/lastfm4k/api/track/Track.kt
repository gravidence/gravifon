package org.gravidence.lastfm4k.api.track

data class Track(
    val artist: String,
    val track: String,
    val album: String? = null,
    val albumArtist: String? = null,
    val trackNumber: String? = null,
    /**
     * The MusicBrainz Track ID.
     */
    val mbId: String? = null,
    /**
     * The length of the track in seconds.
     */
    val duration: Long? = null
)

val VARIOUS_ARTISTS_VARIATIONS = setOf("VA", "V/A", "V.A.")
const val VARIOUS_ARTISTS_STANDARD = "Various Artists"

/**
 * Apply workarounds to make track data comply to Last.fm (sometimes controversial) limitations.
 */
fun Track.comply(): Track {
    return if (albumArtist in VARIOUS_ARTISTS_VARIATIONS) {
        copy(albumArtist = VARIOUS_ARTISTS_STANDARD)
    } else {
        this
    }
}