package org.gravidence.gravifon.plugin.scrobble.lastfm.exception

import org.gravidence.gravifon.plugin.scrobble.lastfm.api.error.ErrorResponse

class LastfmErrorException(val error: ErrorResponse) : LastfmException("Error response received ($error)")