package org.gravidence.lastfm4k.exception

import org.gravidence.lastfm4k.api.error.ErrorApiResponse

/**
 * An exception caused by Last.fm API usage (when response body contains error details payload).
 */
class LastfmApiException(val response: ErrorApiResponse) : LastfmException("[Code ${response.error.code}] ${response.message}")