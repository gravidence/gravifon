package org.gravidence.lastfm4k.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class Token(val token: String)