package org.gravidence.gravifon.domain.album

import org.gravidence.gravifon.domain.track.VirtualTrack

object AlbumScanner {

    fun fullScan(tracks: List<VirtualTrack>): List<VirtualAlbum> {
        val albums = mutableMapOf<String, MutableList<VirtualTrack>>()

        tracks.forEach {
            albums.computeIfAbsent(calculateAlbumKey(it)) { mutableListOf() }
                .add(it)
        }

        return albums.map { VirtualAlbum(it.key, it.value) }
    }

    fun slidingScan(tracks: List<VirtualTrack>): List<VirtualAlbum> {
        val albums = mutableMapOf<String, MutableList<VirtualTrack>>()

        return listOf()
    }

    private fun calculateAlbumKey(track: VirtualTrack): String {
        return track.getAlbum().orEmpty()
    }

}