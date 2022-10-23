package org.gravidence.gravifon.playback.backend.gstreamer

import org.gravidence.gravifon.domain.track.VirtualTrack

class TrackQueue(
    private var activeTrack: VirtualTrack? = null, // queue is inactive initially
    private val nextTracks: ArrayDeque<VirtualTrack> = ArrayDeque()
) {

    private fun makeNextActive(): VirtualTrack? {
        activeTrack = nextTracks.removeFirstOrNull()
        return activeTrack
    }

    fun peekActive(): VirtualTrack? {
        return activeTrack
    }

    fun pollActive(): Pair<VirtualTrack?, VirtualTrack?> {
        return Pair(
            peekActive(),
            makeNextActive()
        )
    }

    fun pushNext(track: VirtualTrack) {
        nextTracks += track
    }

    fun peekNext(): VirtualTrack? {
        return nextTracks.firstOrNull()
    }

    fun pollNext(): VirtualTrack? {
        val nextTrack = nextTracks.firstOrNull()
        nextTracks.clear()
        return nextTrack
    }

}