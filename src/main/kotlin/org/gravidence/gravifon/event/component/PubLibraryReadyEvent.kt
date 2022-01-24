package org.gravidence.gravifon.event.component

import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.plugin.library.Library

class PubLibraryReadyEvent(val library: Library): Event {
}