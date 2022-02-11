package org.gravidence.gravifon.plugin.scrobble.lastfm.api.error

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: Int, val message: String)
