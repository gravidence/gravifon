package org.gravidence.lastfm4k.api.track

data class Scrobble(
    val track: Track,
    /**
     * The time the track started playing, in UNIX timestamp format (integer number of seconds since 00:00:00, January 1st 1970 UTC).
     * This must be in the UTC time zone.
     */
    val timestamp: Long
)
