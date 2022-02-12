package org.gravidence.gravifon.orchestration

import org.gravidence.gravifon.plugin.library.Library

interface LibraryConsumer {

    fun libraryReady(library: Library)

}