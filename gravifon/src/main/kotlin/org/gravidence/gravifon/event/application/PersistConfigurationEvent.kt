package org.gravidence.gravifon.event.application

import org.gravidence.gravifon.event.Event

/**
 * Represents occasional need in persisting configuration. Shouldn't be emitted during shutdown routine.
 */
class PersistConfigurationEvent: Event