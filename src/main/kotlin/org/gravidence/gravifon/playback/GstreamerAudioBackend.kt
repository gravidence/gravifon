package org.gravidence.gravifon.playback

import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.State
import org.freedesktop.gstreamer.Version
import org.freedesktop.gstreamer.elements.PlayBin
import org.gravidence.gravifon.domain.VirtualTrack
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class GstreamerAudioBackend : AudioBackend {

    private val playbin: PlayBin

    init {
        Gst.init(Version.BASELINE)
        playbin = PlayBin("Gravifon")
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
        playbin.stop()
        playbin.setURI(track.uri())
        playbin.pause()
        println(playbin.state)
        println(queryPosition())
        println(queryLength())
    }

    override fun prepareNext(track: VirtualTrack) {
        TODO("Not yet implemented")
    }

    override fun isInitialized(): Boolean {
        TODO("Not yet implemented")
    }

}