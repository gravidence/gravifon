package org.gravidence.gravifon.playback

import mu.KotlinLogging
import org.freedesktop.gstreamer.*
import org.freedesktop.gstreamer.elements.PlayBin
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private val logger = KotlinLogging.logger {}

/**
 * https://gstreamer.freedesktop.org/documentation/playback/playbin.html#playbin-page
 */
@Component
class GstreamerAudioBackend : AudioBackend {

    private val playbin: PlayBin

    private var nextTrack: VirtualTrack? = null

    init {
        Gst.init(Version.BASELINE)
        playbin = PlayBin("Gravifon")

        logger.info { "Audio backend initialized" }
    }

    override fun registerCallback(
        aboutToFinishCallback: () -> Unit,
        audioStreamChangedCallback: (VirtualTrack?) -> Unit,
        endOfStreamCallback: () -> Unit
    ) {
        logger.debug { "Register callbacks" }

        playbin.connect(PlayBin.ABOUT_TO_FINISH {
            logger.debug { "Playing stream is about to finish" }
            aboutToFinishCallback()
        })
        logger.debug { "Callback 'about-to-finish' registered" }

        playbin.connect(PlayBin.AUDIO_CHANGED {
            logger.debug { "Audio stream changed. Now points to ${it.get("current-uri")}" }
            audioStreamChangedCallback(nextTrack)
        })
        logger.debug { "Callback 'audio-changed' registered" }

        playbin.bus.connect(Bus.EOS {
            logger.debug { "End of stream reached" }
            endOfStreamCallback()
        })

        playbin.bus.connect(Bus.BUFFERING { source, percent ->
            logger.warn { "Stream buffering is happening (source=$source percent=$percent), please handle me" }
        })
    }

    override fun play() {
        playbin.play().also {
            logger.debug { "Playback state change result is $it" }
            if (it == StateChangeReturn.FAILURE) {
                logger.warn { "Unable to play stream" }
            }
        }
    }

    override fun pause() {
        when (playbin.state) {
            State.PLAYING -> playbin.pause()
            State.PAUSED -> playbin.play()
            else -> {
                // keep current state
            }
        }
    }

    override fun stop() {
        playbin.stop()
    }

    override fun queryLength(): Duration? {
        return if (!playbin.isPlaying) {
            null
        } else {
            playbin.queryDuration(TimeUnit.MILLISECONDS).also {
                if (it == 0L) logger.warn { "Gstreamer reports stream duration is zero" }
            }.toDuration(DurationUnit.MILLISECONDS)
        }
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