package org.gravidence.gravifon.orchestration.marker

interface ShutdownAware {

    /**
     * Component shutdown routine. EventBus usage is not recommended at that stage.
     */
    fun beforeShutdown()

}