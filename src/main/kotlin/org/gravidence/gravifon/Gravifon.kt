package org.gravidence.gravifon

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mu.KotlinLogging
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.orchestration.Orchestrator
import org.gravidence.gravifon.playback.PlaybackState
import org.gravidence.gravifon.playback.Player
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.plugin.library.Library
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.view.View
import org.slf4j.bridge.SLF4JBridgeHandler
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

private val logger = KotlinLogging.logger {}

object Gravifon {

    val scopeDefault = CoroutineScope(Dispatchers.Default)
    val scopeIO = CoroutineScope(Dispatchers.IO)

    val ctx: ApplicationContext

    val player: Player
    val library: Library
    val orchestrator: Orchestrator

    val activeView: MutableState<View?> = mutableStateOf(null)
    val activePlaylist: MutableState<Playlist?> = mutableStateOf(null)
    val activeVirtualTrack: MutableState<VirtualTrack?> = mutableStateOf(null)

    val playbackState: MutableState<PlaybackState> = mutableStateOf(PlaybackState.STOPPED)
    val playbackPositionState: PlaybackPositionState = PlaybackPositionState()

    init {
        logger.debug { "Initialize SLF4J bridge handler" }
        SLF4JBridgeHandler.removeHandlersForRootLogger()
        SLF4JBridgeHandler.install()

        logger.debug { "Initialize Spring application context" }
        ctx = AnnotationConfigApplicationContext()
        ctx.scan("org.gravidence.gravifon")
        ctx.refresh()

        // TODO these shouldn't be needed when time comes?
        logger.debug { "Expose global beans" }
        player = ctx.getBean<Player>()
        library = ctx.getBean<Library>()
        orchestrator = ctx.getBean<Orchestrator>()
    }

    fun kickoff() {
        logger.info { "Spin-up application" }
        orchestrator.startup()
    }

}