package org.gravidence.gravifon.playback

import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.playback.*
import org.gravidence.gravifon.event.playlist.SubPlaylistPlayNextEvent
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.orchestration.marker.ShutdownAware
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.util.DurationUtil
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class Player(private val audioBackend: AudioBackend, private val audioFlow: AudioFlow) : EventAware, ShutdownAware {

    private var timer = Timer()

    override fun consume(event: Event) {
        when (event) {
            is SubPlaybackStartEvent -> {
                start(event.track)
            }
            is SubPlaybackPauseEvent -> {
                if (GravifonContext.playbackState.value != PlaybackState.STOPPED) {
                    pause()
                }
            }
            is SubPlaybackStopEvent -> {
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

    init {
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

                nextTrack?.let {
                    logger.debug { "Switch over to next track: $it" }
                    GravifonContext.activeVirtualTrack.value = it
                    publish(PubTrackStartEvent(it))
                } // if there's no next track, endOfStreamCallback will handle post-playback state clean-up
            },
            endOfStreamCallback = {
                publish(SubPlaybackStopEvent())
            },
            playbackFailureCallback = { duration ->
                stop()

                GravifonContext.activeVirtualTrack.value?.let {
                    logger.error { "Track playback failure: $it" }

                    if (duration > Duration.ZERO) {
                        publish(PubTrackFinishEvent(it, duration))
                    }
                }

                GravifonContext.activePlaylist.value?.let {
                    publish(SubPlaylistPlayNextEvent(it))
                }
            }
        )
    }

    override fun beforeShutdown() {
        stop()
    }

    private fun start(track: VirtualTrack, forcePlay: Boolean = true) {
        if (forcePlay) {
            // do clean-up (timer, etc)
            stop()
        }

        audioBackend.prepareNext(track)

        if (forcePlay) {
            audioBackend.play().also {
                GravifonContext.playbackState.value = it
            }

            timer = fixedRateTimer(initialDelay = 1000, period = 100) {
                logger.trace { "Time to send playback status update events" }
                updatePlaybackPositionState()
            }
        }
    }

    private fun pause() {
        audioBackend.pause().also {
            GravifonContext.playbackState.value = it
        }
    }

    private fun stop() {
        timer.cancel()

        audioBackend.stop().also {
            GravifonContext.playbackState.value = it
        }

        GravifonContext.activeVirtualTrack.value = null
        updatePlaybackPositionState(Duration.ZERO, Duration.ZERO)
    }

    private fun seek(position: Duration) {
        audioBackend.adjustPosition(position)

        updatePlaybackPositionState(runningPositionOverride = position)
    }

    private fun updatePlaybackPositionState(
        runningPositionOverride: Duration? = null,
        endingPositionOverride: Duration? = null
    ) {
        GravifonContext.playbackPositionState.value = PlaybackPositionState(
            runningPosition = runningPositionOverride ?: audioBackend.queryPosition(),
            endingPosition = endingPositionOverride ?: audioBackend.queryLength()
        )
    }

}