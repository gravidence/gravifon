package org.gravidence.gravifon.playback

import mu.KotlinLogging
import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.State
import org.freedesktop.gstreamer.Version
import org.freedesktop.gstreamer.elements.PlayBin
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@Component
class GstreamerAudioBackend : AudioBackend {

    private val playbin: PlayBin

    init {
        Gst.init(Version.BASELINE)
        playbin = PlayBin("Gravifon")

        logger.info { "Audio backend initialized" }
    }

    override fun play() {
        playbin.play()
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

    override fun queryLength(): Long {
        return playbin.queryDuration(TimeUnit.MILLISECONDS)
    }

    override fun queryPosition(): Long {
        return playbin.queryPosition(TimeUnit.MILLISECONDS)
    }

    override fun adjustPosition(position: Long) {
        playbin.seek(position, TimeUnit.MILLISECONDS)
    }

    override fun queryVolume(): Int {
        return playbin.volume.toInt()
    }

    override fun adjustVolume(volume: Int) {
        playbin.volume = volume.toDouble()
    }

    override fun prepare(track: VirtualTrack) {
        logger.debug { "Prepare to play $track" }

        playbin.stop()
        playbin.setURI(track.uri())
        playbin.pause()

        logger.debug { "Ready to play $track" }
    }

    override fun prepareNext(track: VirtualTrack) {
        TODO("Not yet implemented")
    }

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }

}