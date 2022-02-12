package org.gravidence.lastfm4k.api.auth

import kotlinx.serialization.Serializable

@Serializable
data class Session(val name: String, val key: String, val subscriber: Int)