package org.gravidence.gravifon.plugin.scrobble.lastfm.misc.serialization

import kotlinx.serialization.json.Json

val lastfmSerializer = Json { ignoreUnknownKeys = true }