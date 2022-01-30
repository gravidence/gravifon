package org.gravidence.gravifon.playback

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
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

    override fun consume(event: Event) {
        when (event) {
            is SubPlaybackStatusEvent -> {
                sendStatusUpdate()
            }
            is SubPlaybackStartEvent -> {
                GravifonContext.playbackState.value = PlaybackState.PLAYING
                start(event.track)
            }
            is SubPlaybackPauseEvent -> {
                GravifonContext.playbackState.value = PlaybackState.PAUSED
                pause()
            }
            is SubPlaybackStopEvent -> {
                GravifonContext.playbackState.value = PlaybackState.STOPPED
                stop()
            }
            is SubPlaybackPositionEvent -> {
                seek(event.position)
            }
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
        audioBackend.prepare(track)

        GravifonContext.activeVirtualTrack.value = track

        audioBackend.play()

        // launch with a small delay to workaround gstreamer query_duration API limitations
        GravifonContext.scopeDefault.launch {
            delay(50)
            var trackLength = audioBackend.queryLength()
            if (trackLength == Duration.ZERO) {
                logger.warn { "Audio backend reports stream duration is zero (even after entering PLAYING state). Last chance to make it work by waiting a bit once again and re-query" }
                delay(20)
                trackLength = audioBackend.queryLength()
            }

            GravifonContext.playbackPositionState.endingPosition.value = trackLength
        }

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

        GravifonContext.activeVirtualTrack.value?.let { publish(PubTrackFinishEvent(it)) }

        GravifonContext.activeVirtualTrack.value = null
        GravifonContext.playbackPositionState.runningPosition.value = Duration.ZERO
        GravifonContext.playbackPositionState.endingPosition.value = Duration.ZERO
    }

    private fun seek(position: Duration) {
        audioBackend.adjustPosition(position)

        GravifonContext.playbackPositionState.runningPosition.value = position
    }

    private fun sendStatusUpdate() {
        GravifonContext.playbackPositionState.runningPosition.value = audioBackend.queryPosition()
    }

}