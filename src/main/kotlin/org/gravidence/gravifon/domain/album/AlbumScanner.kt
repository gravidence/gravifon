package org.gravidence.gravifon.domain.album

import org.gravidence.gravifon.domain.track.VirtualTrack

object AlbumScanner {

    /**
     * Discovers all present albums in a track list.
     */
    fun fullScan(tracks: List<VirtualTrack>): List<VirtualAlbum> {
        val albums = mutableMapOf<String, MutableList<VirtualTrack>>()

        tracks.forEach {
            albums.computeIfAbsent(calculateAlbumKey(it)) { mutableListOf() } += it
        }

        return albums.map { VirtualAlbum(it.key, it.value) }
    }

    /**
     * Groups track sequences by album, preserving the order.
     */
    fun slidingScan(tracks: List<VirtualTrack>): List<VirtualAlbum> {
        val albums = mutableListOf<VirtualAlbum>()

        var runningAlbum: VirtualAlbum? = null
        for (track in tracks) {
            val candidateAlbumKey = calculateAlbumKey(track)

            if (candidateAlbumKey != runningAlbum?.albumKey) {
                runningAlbum = VirtualAlbum(candidateAlbumKey, mutableListOf())
                albums += runningAlbum
            }

            runningAlbum.tracks += track
        }

        return albums
    }

    private fun calculateAlbumKey(track: VirtualTrack): String {
        return track.getAlbum().orEmpty()
    }

}