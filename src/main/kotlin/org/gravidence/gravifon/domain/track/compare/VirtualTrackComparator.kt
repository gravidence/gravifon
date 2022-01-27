package org.gravidence.gravifon.domain.track.compare

import org.gravidence.gravifon.domain.track.VirtualTrack

object VirtualTrackComparator {

    /**
     * Builds a [VirtualTrack] comparator based on sequence of [selectors].
     */
    fun build(selectors: List<VirtualTrackSelectors> = listOf(VirtualTrackSelectors.URI)): Comparator<VirtualTrack> {
        var comparator = compareBy((selectors.firstOrNull() ?: VirtualTrackSelectors.URI).selector)
        selectors.drop(1).forEach {
            comparator = comparator.thenBy(it.selector)
        }
        return comparator
    }

}