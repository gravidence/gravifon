package org.gravidence.lastfm4k.misc

import kotlinx.serialization.json.Json

val lastfmSerializer = Json { ignoreUnknownKeys = true }