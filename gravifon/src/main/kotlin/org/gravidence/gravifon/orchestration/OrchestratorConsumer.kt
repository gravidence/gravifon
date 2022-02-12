package org.gravidence.gravifon.orchestration

interface OrchestratorConsumer {

    /**
     * Core component startup routine. EventBus usage is not recommended at that stage.
     */
    fun startup()

    /**
     * Regular component startup routine. EventBus usage is not recommended at that stage.
     */
    fun afterStartup()

    /**
     * Component shutdown routine. EventBus usage is not recommended at that stage.
     */
    fun beforeShutdown()

}