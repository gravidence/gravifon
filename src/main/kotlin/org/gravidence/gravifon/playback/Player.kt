package org.gravidence.gravifon.playback

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.gravidence.gravifon.Gravifon
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
import kotlin.time.Duration

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

    override fun startup() {
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
        audioBackend.play()

        // launch with a small delay to workaround gstreamer query_duration API limitations
        Gravifon.scopeDefault.launch {
            delay(50)
            var trackLength = audioBackend.queryLength()
            if (trackLength == Duration.ZERO) {
                logger.warn { "Audio backend reports stream duration is zero (even after entering PLAYING state). Last chance to make it work by waiting a bit once again and re-query" }
                delay(20)
                trackLength = audioBackend.queryLength()
            }
            publish(PubPlaybackStartEvent(track, trackLength))
            publish(PubTrackStartEvent(track))
        }

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

    private fun seek(position: Duration) {
        audioBackend.adjustPosition(position)
        // not sending status update event since position change already initiated by UI, and there's no other party that could do so
    }

    private fun sendStatusUpdate() {
        publish(PubPlaybackPositionEvent(audioBackend.queryPosition()))
    }

}