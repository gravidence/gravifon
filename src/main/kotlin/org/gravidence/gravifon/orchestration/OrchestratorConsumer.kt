package org.gravidence.gravifon.orchestration

interface OrchestratorConsumer {

    fun boot()
    fun afterStartup()
    fun beforeShutdown()

}