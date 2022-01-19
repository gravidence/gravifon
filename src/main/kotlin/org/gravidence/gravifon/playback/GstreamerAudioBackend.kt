package org.gravidence.gravifon.playback

import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.State
import org.freedesktop.gstreamer.Version
import org.freedesktop.gstreamer.elements.PlayBin
import org.gravidence.gravifon.domain.VirtualTrack
import org.springframework.stereotype.Component

@Component
class GstreamerAudioBackend : AudioBackend {

    private val playbin: PlayBin

    init {
        Gst.init(Version.BASELINE)
        playbin = PlayBin("Gravifon")
    }

    override fun play(track: VirtualTrack) {
        println("Play $track")
        playbin.setURI(track.uri())
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

    override fun prepareNext() {
        TODO("Not yet implemented")
    }

}