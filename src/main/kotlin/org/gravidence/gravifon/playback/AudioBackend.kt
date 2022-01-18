package org.gravidence.gravifon.playback

import org.jaudiotagger.audio.AudioFile

interface AudioBackend {

    fun play(file: AudioFile)
    fun pause()
    fun stop()

    fun prepareNext()

}