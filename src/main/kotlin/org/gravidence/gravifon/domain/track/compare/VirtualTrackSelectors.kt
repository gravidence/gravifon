package org.gravidence.gravifon.domain.track.compare

import org.gravidence.gravifon.domain.track.VirtualTrack

/**
 * [VirtualTrack] selectors for sort/compare operations.
 */
enum class VirtualTrackSelectors(val selector: (VirtualTrack) -> Comparable<*>?) {

    URI(VirtualTrack::uri),
    ARTIST(VirtualTrack::getArtist),
    ALBUM_ARTIST(VirtualTrack::getAlbumArtist),
    ALBUM(VirtualTrack::getAlbum),
    DATE(VirtualTrack::getDate),
    YEAR(VirtualTrack::getYear),
    TITLE(VirtualTrack::getTitle),
    TRACK(VirtualTrack::getTrack),
    TRACK_TOTAL(VirtualTrack::getTrackTotal),
    DISC(VirtualTrack::getDisc),
    DISC_TOTAL(VirtualTrack::getDiscTotal),

}