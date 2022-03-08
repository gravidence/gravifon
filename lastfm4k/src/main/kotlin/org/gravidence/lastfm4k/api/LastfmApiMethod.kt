package org.gravidence.lastfm4k.api

enum class LastfmApiMethod(val value: String) {

    AUTH_GETTOKEN("auth.getToken"),
    AUTH_GETSESSION("auth.getSession"),

    TRACK_UPDATENOWPLAYING("track.updateNowPlaying"),
    TRACK_SCROBBLE("track.scrobble"),
    TRACK_GETINFO("track.getInfo"),

    USER_GETINFO("user.getInfo"),

}