package org.gravidence.lastfm4k.api

import org.gravidence.lastfm4k.api.auth.Session

class LastfmApiContext(
    val client: LastfmApiClient,
    var session: Session? = null
)