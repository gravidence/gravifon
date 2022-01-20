package org.gravidence.gravifon.domain.album

import org.gravidence.gravifon.domain.track.VirtualTrack

data class VirtualAlbum(val album: String, val tracks: MutableList<VirtualTrack> = mutableListOf()) {

}