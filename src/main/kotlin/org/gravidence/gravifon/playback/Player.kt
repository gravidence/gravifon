package org.gravidence.gravifon.playback

import org.gravidence.gravifon.domain.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.EventConsumer
import org.gravidence.gravifon.event.playback.*
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.springframework.stereotype.Component
import java.util.*
import kotlin.concurrent.fixedRateTimer

@Component
class Player(private val audioBackend: AudioBackend) : EventConsumer() {

    private var timer = Timer()

    private var currentTrack: VirtualTrack? = null

    private fun playbackStart(event: SubPlaybackStartEvent) {
        println("Playback start event")
        currentTrack = event.track

        audioBackend.prepare(event.track)

        EventBus.publish(PubPlaybackStartEvent(event.track, audioBackend.queryLength()))

        timer = fixedRateTimer(initialDelay = 1000, period = 100) {
//            println(refreshEvent.track.uri())
            sendStatusUpdate()
        }

        audioBackend.play()

        EventBus.publish(PubTrackStartEvent(event.track))
    }

    private fun playbackPause() {
        println("Playback pause event")
        audioBackend.pause()
    }

    private fun playbackStop() {
        timer.cancel()

        println("Playback stop")
        audioBackend.stop()

        currentTrack?.let { EventBus.publish(PubTrackFinishEvent(it)) }
    }

    override fun consume(event: Event) {
        when (event) {
            is SubPlaybackStatusEvent -> sendStatusUpdate()
            is SubPlaybackStartEvent -> playbackStart(event)
            is SubPlaybackPauseEvent -> playbackPause()
            is SubPlaybackStopEvent -> playbackStop()
        }
    }

    private fun sendStatusUpdate() {
        EventBus.publish(PubPlaybackPositionEvent(audioBackend.queryPosition()))
    }

}