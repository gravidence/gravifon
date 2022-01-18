package org.gravidence.gravifon.playback

import org.freedesktop.gstreamer.Gst
import org.freedesktop.gstreamer.State
import org.freedesktop.gstreamer.Version
import org.freedesktop.gstreamer.elements.PlayBin
import org.jaudiotagger.audio.AudioFile
import org.springframework.stereotype.Component

@Component
class GstreamerAudioBackend : AudioBackend {

    private val playbin: PlayBin

    init {
        Gst.init(Version.BASELINE)
        playbin = PlayBin("Gravifon")
    }

    override fun play(file: AudioFile) {
//        val file = File("/home/m2/Library/Paul van Dyk - Amanecer.flac")
//        val audioFile = AudioFileIO.read(file)
        println("Play $file")
        playbin.setInputFile(file.file)
        playbin.state = State.PLAYING
    }

    override fun pause() {
        when (playbin.state) {
            State.PLAYING -> playbin.state = State.PAUSED
            State.PAUSED -> playbin.state = State.PLAYING
            else -> {
                // keep current state
            }
        }
    }

    override fun stop() {
        playbin.state = State.NULL
    }

    override fun prepareNext() {
        TODO("Not yet implemented")
    }

}