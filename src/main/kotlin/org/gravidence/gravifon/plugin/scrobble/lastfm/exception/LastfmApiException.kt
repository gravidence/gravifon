package org.gravidence.gravifon.plugin.scrobble.lastfm.exception

import org.gravidence.gravifon.plugin.scrobble.lastfm.api.error.ErrorResponse

/**
 * An exception caused by Last.fm API usage (when response body contains error details payload).
 */
class LastfmApiException(val error: ErrorResponse) : LastfmException("Error response received ('${error.message}')")