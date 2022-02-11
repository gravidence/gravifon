package org.gravidence.gravifon.plugin.scrobble.lastfm.api

enum class LastfmApiMethod(val value: String) {

    AUTH_GETTOKEN("auth.getToken"),
    AUTH_GETSESSION("auth.getSession"),

    TRACK_UPDATENOWPLAYING("track.updateNowPlaying"),
    TRACK_SCROBBLE("track.scrobble"),

}