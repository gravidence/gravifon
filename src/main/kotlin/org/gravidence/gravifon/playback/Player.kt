package org.gravidence.gravifon.playback

import mu.KotlinLogging
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventHandler
import org.gravidence.gravifon.event.playback.*
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.gravidence.gravifon.orchestration.OrchestratorConsumer
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.fixedRateTimer

private val logger = KotlinLogging.logger {}

@Component
class Player(private val audioBackend: AudioBackend) : EventHandler(), OrchestratorConsumer {

    private var timer = Timer()

    private var currentTrack: VirtualTrack? = null

    override fun consume(event: Event) {
        when (event) {
            is SubPlaybackStatusEvent -> sendStatusUpdate()
            is SubPlaybackStartEvent -> start(event.track)
            is SubPlaybackPauseEvent -> pause()
            is SubPlaybackStopEvent -> stop()
            is SubPlaybackPositionEvent -> seek(event.position)
        }
    }

    override fun boot() {
        // do nothing
    }

    override fun afterStartup() {
        // do nothing?
    }

    override fun beforeShutdown() {
        stop()
    }

    private fun start(track: VirtualTrack) {
        currentTrack = track

        audioBackend.prepare(track)
        publish(PubPlaybackStartEvent(track, audioBackend.queryLength()))

        audioBackend.play()
        publish(PubTrackStartEvent(track))

        timer = fixedRateTimer(initialDelay = 1000, period = 100) {
            logger.trace { "Time to send playback status update events" }
            sendStatusUpdate()
        }
    }

    private fun pause() {
        audioBackend.pause()
    }

    private fun stop() {
        timer.cancel()

        audioBackend.stop()

        currentTrack?.let { publish(PubTrackFinishEvent(it)) }
    }

    private fun seek(position: Long) {
        audioBackend.adjustPosition(position)
        // not sending status update event since position change already initiated by UI, and there's no other party that could do so
    }

    private fun sendStatusUpdate() {
        publish(PubPlaybackPositionEvent(audioBackend.queryPosition()))
    }

}