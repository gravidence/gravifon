package org.gravidence.gravifon.plugin.scrobble.lastfm.exception

import org.gravidence.gravifon.plugin.scrobble.lastfm.api.error.ErrorApiResponse

/**
 * An exception caused by Last.fm API usage (when response body contains error details payload).
 */
class LastfmApiException(val response: ErrorApiResponse) : LastfmException("Error response received ('${response.message}')")