package org.gravidence.lastfm4k.exception

import kotlinx.serialization.SerializationException

/**
 * Json de/serialization exception.
 */
class LastfmSerializationException(val json: String, val reason: SerializationException) : LastfmException("Failed to process: json=$json, exception=${reason.message}")