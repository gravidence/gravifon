package org.gravidence.lastfm4k.exception

/**
 * Generic Last.fm plugin exception (when something unexpected happens). Not necessarily a bug, but also something like network error.
 */
open class LastfmException(msg: String) : Exception(msg)