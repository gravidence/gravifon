package org.gravidence.gravifon.domain.track.format

import org.gravidence.gravifon.domain.track.VirtualTrack

/**
 * [VirtualTrack] selectors extract field values.
 */
enum class VirtualTrackFormatSelectors(val selector: (VirtualTrack) -> Any?) {

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
    DURATION_SHORT(VirtualTrack::getLengthFormatShortHours),

}