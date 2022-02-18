package org.gravidence.gravifon.orchestration.marker

/**
 * Represents a component which has to do some actions before application shutdown.
 */
interface ShutdownAware {

    /**
     * Component shutdown routine. EventBus usage is not recommended at that stage.
     */
    fun beforeShutdown()

}