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