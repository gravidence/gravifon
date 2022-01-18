package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.PhysicalTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventConsumer
import org.gravidence.gravifon.event.playback.PlaybackPauseEvent
import org.gravidence.gravifon.event.playback.PlaybackStartEvent
import org.gravidence.gravifon.event.playback.PlaybackStopEvent
import org.gravidence.gravifon.library.Library
import org.springframework.stereotype.Component

@Component
class Player(private val audioBackend: AudioBackend, private val library: Library) : EventConsumer() {

    fun playbackStart(event: PlaybackStartEvent) {
        println("Playback start event")
        val track = PhysicalTrack(library.random().path).file
        audioBackend.play(track)
        // TODO do STOP first
    }

    fun playbackPause(event: PlaybackPauseEvent) {
        println("Playback pause event")
        audioBackend.pause()
    }

    fun playbackStop(event: PlaybackStopEvent) {
        println("Playback stop")
        audioBackend.stop()
    }

    override fun consume(event: Event) {
        when (event) {
            is PlaybackStartEvent -> playbackStart(event)
            is PlaybackPauseEvent -> playbackPause(event)
            is PlaybackStopEvent -> playbackStop(event)
        }
    }

}