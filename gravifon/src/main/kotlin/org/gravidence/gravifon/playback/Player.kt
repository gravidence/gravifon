package org.gravidence.gravifon.playback

import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.domain.notification.NotificationLifespan
import org.gravidence.gravifon.domain.notification.NotificationType
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PushInnerNotificationEvent
import org.gravidence.gravifon.event.playback.*
import org.gravidence.gravifon.event.playlist.PlayNextFromPlaylistEvent
import org.gravidence.gravifon.event.track.TrackFinishedEvent
import org.gravidence.gravifon.event.track.TrackStartedEvent
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
            is StartPlaybackEvent -> {
                start(event.track)
            }
            is PausePlaybackEvent -> {
                if (GravifonContext.playbackState.value != PlaybackState.STOPPED) {
                    pause()
                }
            }
            is StopPlaybackEvent -> {
                stop()
            }
            is RepositionPlaybackPointAbsoluteEvent -> {
                seek(event.position)
            }
            is RepositionPlaybackPointRelativeEvent -> {
                seek(DurationUtil.max(Duration.ZERO, audioBackend.queryPosition() + event.positionDelta))
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
            audioStreamChangedCallback = { playedTrack, nextTrack, duration ->
                logger.debug { "Enter audio stream changed callback: playedTrack=$playedTrack, playtime=$duration, next=$nextTrack" }

                playedTrack?.let {
                    if (duration > Duration.ZERO) {
                        publish(TrackFinishedEvent(it, duration))
                    }
                }

                nextTrack?.let {
                    logger.debug { "Switch over to next track: $it" }
                    GravifonContext.setActiveTrack(it)
                    publish(TrackStartedEvent(it))
                } // if there's no next track, endOfStreamCallback will handle post-playback state clean-up
            },
            audioStreamBufferingCallback = {
                publish(
                    PushInnerNotificationEvent(
                        Notification(
                            message = "Buffering audio stream, $it%...",
                            type = NotificationType.MINOR,
                            lifespan = NotificationLifespan.SHORT
                        )
                    )
                )
            },
            endOfStreamCallback = {
                publish(StopPlaybackEvent())
            },
            playbackFailureCallback = { playedTrack, nextTrack, duration ->
                // report playback failure for either already active or next (failed to start) track
                (playedTrack ?: nextTrack)?.let {
                    val message = "Playback failure: ${it.uri()}"
                    logger.error { message }
                    publish(
                        PushInnerNotificationEvent(
                            Notification(
                                message = message,
                                type = NotificationType.ERROR,
                                lifespan = NotificationLifespan.LONG
                            )
                        )
                    )

                    it.failing = true

                    if (duration > Duration.ZERO) {
                        publish(TrackFinishedEvent(it, duration))
                    }
                }

                stop()

                GravifonContext.activePlaylist.value?.let {
                    publish(PlayNextFromPlaylistEvent(it))
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

            timer = fixedRateTimer(initialDelay = 1000, period = 200) {
                logger.trace { "Ask components to update playback status" }
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

        GravifonContext.setActiveTrack(null)
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