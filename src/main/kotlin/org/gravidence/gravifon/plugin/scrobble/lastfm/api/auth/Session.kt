package org.gravidence.gravifon.plugin.scrobble.lastfm.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class Session(val name: String, val key: String, val subscriber: Int)