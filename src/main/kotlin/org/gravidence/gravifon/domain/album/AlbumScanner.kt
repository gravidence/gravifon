package org.gravidence.gravifon.domain.album

import org.gravidence.gravifon.domain.track.VirtualTrack

const val NO_ALBUM_KEY = ""

object AlbumScanner {

    /**
     * Discovers all present albums in track list.
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

    /**
     * Calculates a unique album key for [track].
     */
    // TODO add support of MusicBrainz tags
    fun calculateAlbumKey(track: VirtualTrack): String {
        if (track.getAlbum() == null) {
            return NO_ALBUM_KEY
        }

        return listOfNotNull(track.getAlbum(), track.getAlbumArtist()).joinToString(separator = "::")
    }

}