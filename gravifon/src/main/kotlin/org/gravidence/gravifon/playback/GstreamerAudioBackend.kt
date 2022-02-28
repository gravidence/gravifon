package org.gravidence.gravifon.playback

import mu.KotlinLogging
import org.freedesktop.gstreamer.*
import org.freedesktop.gstreamer.elements.PlayBin
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.util.Stopwatch
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

/**
 * Gstreamer Playbin based audio backend.
 * See [documentation](https://gstreamer.freedesktop.org/documentation/playback/playbin.html#playbin-page).
 */
@Component
class GstreamerAudioBackend : AudioBackend {

    private val playbin: PlayBin

    private var stopwatch: Stopwatch = Stopwatch()

    private var nextTrack: VirtualTrack? = null

    init {
        Gst.init(Version.BASELINE)
        playbin = PlayBin("Gravifon")

        logger.info { "Audio backend initialized" }
    }

    override fun registerCallback(
        aboutToFinishCallback: () -> Unit,
        audioStreamChangedCallback: (VirtualTrack?, Duration) -> Unit,
        endOfStreamCallback: () -> Unit,
        playbackFailureCallback: (Duration) -> Unit,
    ) {
        logger.debug { "Register callbacks" }

        playbin.connect(PlayBin.ABOUT_TO_FINISH {
            logger.debug { "Playing stream is about to finish" }
            aboutToFinishCallback()
        })
        logger.debug { "Callback 'about-to-finish' registered" }

        playbin.connect(PlayBin.AUDIO_CHANGED {
            // print message only when there's actual next track, otherwise AUDIO_CHANGE event means playback is being stopped
            if (nextTrack != null) {
                logger.debug { "Audio stream changed. Now points to ${it.get("current-uri")}" }
            }

            audioStreamChangedCallback(nextTrack, stopwatch.stop())
            stopwatch.count()
        })
        logger.debug { "Callback 'audio-changed' registered" }

        playbin.bus.connect(Bus.EOS {
            logger.debug { "End of stream reached" }
            endOfStreamCallback()
        })

        playbin.bus.connect(Bus.BUFFERING { source, percent ->
            logger.warn { "Stream buffering is happening (source=$source percent=$percent), please handle me" }
        })

        playbin.bus.connect(Bus.WARNING { source, code, message ->
            logger.warn { "Gstreamer warning occurred: code=$code, message=$message" }
        })

        playbin.bus.connect(Bus.ERROR { source, code, message ->
            logger.error { "Gstreamer error occurred: code=$code, message=$message" }

            // stream doesn't contain enough data
            if (code == 4) {
                playbackFailureCallback(stopwatch.stop())
            }
        })
    }

    override fun play(): PlaybackState {
        playbin.play().also {
            if (it == StateChangeReturn.FAILURE) {
                logger.warn { "Unable to play stream" }
                return PlaybackState.STOPPED
            }
        }

        return PlaybackState.PLAYING
    }

    override fun pause(): PlaybackState {
        when (playbin.state) {
            State.PLAYING -> {
                playbin.pause()
                stopwatch.pause()
                return PlaybackState.PAUSED
            }
            State.PAUSED -> {
                return play().also {
                    if (it == PlaybackState.PLAYING) {
                        stopwatch.count()
                    }
                }
            }
            else -> {
                // keep current state
                return PlaybackState.STOPPED
            }
        }
    }

    override fun stop(): PlaybackState {
        // clear next track, so it won't affect AUDIO_CHANGED event logic
        nextTrack = null

        playbin.stop()

        // make sure stopwatch is stopped (all related logic is executed in AUDIO_CHANGED event handler)
        stopwatch.stop()

        return PlaybackState.STOPPED
    }

    /**
     * Queries actual audio stream length from Gstreamer. The value is cached the framework until audio stream is not changed.
     */
    override fun queryLength(): Duration {
        return playbin.queryDuration(TimeUnit.MILLISECONDS).also {
            if (it == 0L) logger.warn { "Gstreamer reports stream duration is zero" }
        }.toDuration(DurationUnit.MILLISECONDS)
    }

    override fun queryPosition(): Duration {
        return playbin.queryPosition(TimeUnit.MILLISECONDS)
            .toDuration(DurationUnit.MILLISECONDS)
    }

    override fun adjustPosition(position: Duration) {
        playbin.seek(position.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }

    override fun queryVolume(): Int {
        return playbin.volume.toInt()
    }

    override fun adjustVolume(volume: Int) {
        playbin.volume = volume.toDouble()
    }

    override fun prepareNext(track: VirtualTrack) {
        playbin.setURI(track.uri())

        nextTrack = track

        logger.debug { "Next track to play: $track" }
    }

}