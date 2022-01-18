package org.gravidence.gravifon

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.ApplicationStartupEvent
import org.gravidence.gravifon.playback.Player
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

object Gravifon {

    val scopeDefault = CoroutineScope(Dispatchers.Default)
    val scopeIO = CoroutineScope(Dispatchers.IO)

    val ctx: ApplicationContext

    val player: Player

    init {
        println("Initialize Spring Application Context")
        ctx = AnnotationConfigApplicationContext()
        ctx.scan("org.gravidence.gravifon")
        ctx.refresh()

        println("Expose global beans")
        player = ctx.getBean<Player>()
    }

    fun kickoff() {
        EventBus.publish(ApplicationStartupEvent())
    }

}