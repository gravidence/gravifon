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
import org.gravidence.gravifon.playback.backend.AudioBackend
import org.gravidence.gravifon.ui.state.PlaybackPositionState
import org.gravidence.gravifon.util.DurationUtil
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

@Component
class PlaybackManager(private val audioFlow: AudioFlow) : EventAware, ShutdownAware {

    private var audioBackend: AudioBackend? = null

    private var timer = Timer()

    override fun consume(event: Event) {
        when (event) {
            is SelectAudioBackendEvent -> init(event.audioBackend)
            is StartPlaybackEvent -> start(event.track)
            is PausePlaybackEvent -> {
                if (GravifonContext.playbackStatusState.value != PlaybackStatus.STOPPED) {
                    pause()
                }
            }
            is StopPlaybackEvent -> stop()
            is RepositionPlaybackPointAbsoluteEvent -> seek(event.position)
            is RepositionPlaybackPointRelativeEvent -> seek(event.positionDelta, false)
        }
    }

    fun init(audioBackend: AudioBackend) {
        this.audioBackend?.run {
            logger.info { "Shut down active audio backend operations: $id" }
            stop()
        }

        logger.info { "Activate audio backend: ${audioBackend.id}" }

        try {
            audioBackend.init()
            audioBackend.registerCallback(
                aboutToFinishCallback = ::aboutToFinishCallback,
                audioStreamChangedCallback = ::audioStreamChangedCallback,
                audioStreamBufferingCallback = ::audioStreamBufferingCallback,
                endOfStreamCallback = ::endOfStreamCallback,
                playbackFailureCallback = ::playbackFailureCallback
            )

            this.audioBackend = audioBackend
        } catch (e: Exception) {
            logger.error(e) { "Playback manager initialization failed" }
            this.audioBackend = null

            publish(
                PushInnerNotificationEvent(
                    Notification(
                        message = "Failed to initialize audio backend",
                        type = NotificationType.ERROR,
                        lifespan = NotificationLifespan.INFINITE
                    )
                )
            )
        }
    }

    override fun beforeShutdown() {
        stop()
    }

    private fun start(track: VirtualTrack, forcePlay: Boolean = true) {
        if (forcePlay) {
            // do clean-up (timer, etc)
            stop()
        }

        audioBackend?.prepareNext(track)

        if (forcePlay) {
            audioBackend?.play()?.also {
                GravifonContext.playbackStatusState.value = it

                if (it == PlaybackStatus.PLAYING) {
                    timer = fixedRateTimer(initialDelay = 0, period = 200) {
                        logger.trace { "Ask components to update playback status" }
                        updatePlaybackPositionState()
                    }
                }
            }
        }
    }

    private fun pause() {
        audioBackend?.pause()?.also {
            GravifonContext.playbackStatusState.value = it
        }
    }

    private fun stop() {
        timer.cancel()

        audioBackend?.stop()?.also {
            GravifonContext.playbackStatusState.value = it
        }

        GravifonContext.setActiveTrack(null)
        updatePlaybackPositionState(Duration.ZERO, Duration.ZERO)
    }

    private fun seek(position: Duration, isAbsolute: Boolean = true) {
        val pos = if (isAbsolute) {
            position
        } else {
            DurationUtil.max(Duration.ZERO, audioBackend!!.queryPosition() + position)
        }
        audioBackend!!.adjustPosition(pos)

        updatePlaybackPositionState(runningPositionOverride = pos)
    }

    private fun updatePlaybackPositionState(
        runningPositionOverride: Duration? = null,
        endingPositionOverride: Duration? = null
    ) {
        GravifonContext.playbackPositionState.value = PlaybackPositionState(
            runningPosition = runningPositionOverride ?: audioBackend!!.queryPosition(),
            endingPosition = endingPositionOverride ?: audioBackend!!.queryLength()
        )
    }

    private fun aboutToFinishCallback() {
        audioFlow.next()?.let {
            start(it, false)
        }
    }

    private fun audioStreamChangedCallback(played: VirtualTrack?, next: VirtualTrack?, playtime: Duration) {
        logger.debug { "Enter audio stream changed callback: playedTrack=$played, playtime=$playtime, nextTrack=$next" }

        played?.let {
            if (playtime > Duration.ZERO) {
                publish(TrackFinishedEvent(it, playtime))
            }
        }

        next?.let {
            logger.debug { "Switch over to next track: $it" }
            GravifonContext.setActiveTrack(it)
            publish(TrackStartedEvent(it))
        } // if there's no next track, endOfStreamCallback will handle post-playback state clean-up
    }

    private fun audioStreamBufferingCallback(percent: Int) {
        publish(
            PushInnerNotificationEvent(
                Notification(
                    message = "Buffering audio stream, $percent%...",
                    type = NotificationType.MINOR,
                    lifespan = NotificationLifespan.SHORT
                )
            )
        )
    }

    private fun endOfStreamCallback() {
        publish(StopPlaybackEvent())
    }

    private fun playbackFailureCallback(played: VirtualTrack?, next: VirtualTrack?, playtime: Duration) {
        // report playback failure for either already next (failed to start) or active track
        // n.b. next track is not null only at the moment of AUDIO_CHANGE event, so it's likely a culprit if present
        (next ?: played)?.let { track ->
            val message = "Playback failure: ${track.uri()}"
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

            track.failing = true

            if (playtime > Duration.ZERO) {
                publish(TrackFinishedEvent(track, playtime))
            }

            stop()

            GravifonContext.activePlaylist.value?.let { playlist ->
                publish(PlayNextFromPlaylistEvent(playlist))
            }
        }
    }

}