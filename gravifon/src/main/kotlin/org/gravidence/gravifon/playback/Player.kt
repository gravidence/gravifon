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
import org.gravidence.gravifon.util.DurationUtil
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class Player(private val audioBackend: AudioBackend, private val audioFlow: AudioFlow) : EventHandler(), OrchestratorConsumer {

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
                if (GravifonContext.playbackState.value != PlaybackState.STOPPED) {
                    GravifonContext.playbackState.value = PlaybackState.PAUSED
                    pause()
                }
            }
            is SubPlaybackStopEvent -> {
                GravifonContext.playbackState.value = PlaybackState.STOPPED
                stop()
            }
            is SubPlaybackAbsolutePositionEvent -> {
                seek(event.position)
            }
            is SubPlaybackRelativePositionEvent -> {
                seek(DurationUtil.max(Duration.ZERO, audioBackend.queryPosition() + event.position))
            }
        }
    }

    override fun startup() {
        audioBackend.registerCallback(
            aboutToFinishCallback = {
                audioFlow.next()?.let {
                    start(it, false)
                }
            },
            audioStreamChangedCallback = { nextTrack, duration ->
                GravifonContext.activeVirtualTrack.value?.let {
                    if (duration > Duration.ZERO) {
                        publish(PubTrackFinishEvent(it, duration))
                    }
                }

                if (nextTrack != null) {
                    GravifonContext.activeVirtualTrack.value = nextTrack.also {
                        logger.debug { "Switch over to next track: $it" }
                    }

                    // launch with a small delay to workaround gstreamer query_duration API limitations
                    GravifonContext.scopeDefault.launch {
                        if (audioBackend.queryLength() == null) {
                            delay(50)
                        } else {
                            // TODO that's way too much, but for smaller values gstreamer returns prev stream length, investigation needed
                            delay(1500)
                        }
                        var trackLength = audioBackend.queryLength()

                        if (trackLength == Duration.ZERO) {
                            logger.warn { "Audio backend reports stream duration is zero (even after entering PLAYING state). Last chance to make it work by waiting a bit once again and re-query" }
                            delay(20)
                            trackLength = audioBackend.queryLength()
                        }

                        GravifonContext.playbackPositionState.endingPosition.value = (trackLength ?: Duration.ZERO).also {
                            logger.debug { "Calculated track length: $it" }
                        }
                    }

                    publish(PubTrackStartEvent(nextTrack))
                } else {
                    GravifonContext.playbackPositionState.endingPosition.value = Duration.ZERO
                }
            },
            endOfStreamCallback = {
                publish(SubPlaybackStopEvent())
            }
        )
    }

    override fun afterStartup() {
        // do nothing?
    }

    override fun beforeShutdown() {
        stop()
    }

    private fun start(track: VirtualTrack, forcePlay: Boolean = true) {
        // TODO check if really needed
        if (forcePlay) {
            audioBackend.stop()
        }

        audioBackend.prepareNext(track)

        if (forcePlay) {
            audioBackend.play()

            timer = fixedRateTimer(initialDelay = 1000, period = 100) {
                logger.trace { "Time to send playback status update events" }
                sendStatusUpdate()
            }
        }
    }

    private fun pause() {
        audioBackend.pause()
    }

    private fun stop() {
        timer.cancel()

        audioBackend.stop()

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