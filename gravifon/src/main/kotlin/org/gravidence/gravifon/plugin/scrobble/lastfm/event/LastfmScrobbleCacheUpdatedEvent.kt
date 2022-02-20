package org.gravidence.gravifon.plugin.scrobble.lastfm.event

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.plugin.scrobble.Scrobble

class LastfmScrobbleCacheUpdatedEvent(val scrobbleCache: List<Scrobble>) : Event