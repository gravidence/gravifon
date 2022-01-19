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

    private fun start(track: VirtualTrack) {
        println("Playback start event")
        currentTrack = track

        audioBackend.prepare(track)

        EventBus.publish(PubPlaybackStartEvent(track, audioBackend.queryLength()))

        timer = fixedRateTimer(initialDelay = 1000, period = 100) {
//            println(refreshEvent.track.uri())
            sendStatusUpdate()
        }

        audioBackend.play()

        EventBus.publish(PubTrackStartEvent(track))
    }

    private fun pause() {
        println("Playback pause event")
        audioBackend.pause()
    }

    private fun stop() {
        timer.cancel()

        println("Playback stop")
        audioBackend.stop()

        currentTrack?.let { EventBus.publish(PubTrackFinishEvent(it)) }
    }

    private fun seek(position: Long) {
        audioBackend.adjustPosition(position)
        // not sending status update event since position change already initiated by UI, and there's no other party that could do so
    }

    override fun consume(event: Event) {
        when (event) {
            is SubPlaybackStatusEvent -> sendStatusUpdate()
            is SubPlaybackStartEvent -> start(event.track)
            is SubPlaybackPauseEvent -> pause()
            is SubPlaybackStopEvent -> stop()
            is SubPlaybackPositionEvent -> seek(event.position)
        }
    }

    private fun sendStatusUpdate() {
        EventBus.publish(PubPlaybackPositionEvent(audioBackend.queryPosition()))
    }

}