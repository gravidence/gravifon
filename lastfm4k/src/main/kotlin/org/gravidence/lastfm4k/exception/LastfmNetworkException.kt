package org.gravidence.lastfm4k.exception

import org.http4k.core.Response

/**
 * An exception from network layer (no payload received from Last.fm service).
 */
class LastfmNetworkException(val response: Response) : LastfmException("HTTP ${response.status}")