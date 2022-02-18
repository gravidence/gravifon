package org.gravidence.gravifon.orchestration.marker

import org.gravidence.gravifon.configuration.FileStorage

/**
 * Represents a component with state, e.g. playlist file(s).
 */
interface Stateful {

    val fileStorage: FileStorage

}