package org.gravidence.gravifon.plugin.scrobble.lastfm.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class Token(val token: String)