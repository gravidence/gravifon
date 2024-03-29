package org.gravidence.lastfm4k.api

import org.gravidence.lastfm4k.api.auth.Session
import org.gravidence.lastfm4k.api.auth.Token
import org.gravidence.lastfm4k.api.track.Scrobble
import org.gravidence.lastfm4k.api.track.Track
import org.gravidence.lastfm4k.api.user.UserInfo
import org.gravidence.lastfm4k.misc.Param

private fun keyWithSuffix(key: String, index: Int?): String {
    return if (index == null) {
        key
    } else {
        "$key[$index]"
    }
}

fun LastfmApiMethod.paramMethod(): Param {
    return Param("method", value)
}

fun Token.paramToken(): Param {
    return Param("token", token)
}

fun Session.paramSessionKey(): Param {
    return Param("sk", key)
}

fun UserInfo.paramUsername(): Param {
    return Param("username", name)
}

fun Track.paramArtist(index: Int? = null): Param {
    return Param(keyWithSuffix("artist", index), artist)
}

fun Track.paramTrack(index: Int? = null): Param {
    return Param(keyWithSuffix("track", index), track)
}

fun Track.paramAlbum(index: Int? = null): Param? {
    return album?.let { Param(keyWithSuffix("album", index), it) }
}

fun Track.paramAlbumArtist(index: Int? = null): Param? {
    return albumArtist?.let { Param(keyWithSuffix("albumArtist", index), it) }
}

fun Track.paramTrackNumber(index: Int? = null): Param? {
    return trackNumber?.let { Param(keyWithSuffix("trackNumber", index), it) }
}

fun Track.paramMbId(index: Int? = null): Param? {
    return mbId?.let { Param(keyWithSuffix("mbId", index), it) }
}

fun Track.paramDuration(index: Int? = null): Param? {
    return duration?.let { Param(keyWithSuffix("duration", index), it.toString()) }
}

fun Scrobble.paramTimestamp(index: Int? = null): Param {
    return Param(keyWithSuffix("timestamp", index), timestamp.toString())
}