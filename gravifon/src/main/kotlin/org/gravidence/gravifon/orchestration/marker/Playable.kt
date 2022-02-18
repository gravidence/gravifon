package org.gravidence.gravifon.orchestration.marker

import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.playlist.Playlist

private val logger = KotlinLogging.logger {}

/**
 * Represents a component with playlist which is allowed to be a source to playback.
 */
interface Playable {

    val playlist: Playlist

    fun activatePlaylist() {
        GravifonContext.activePlaylist.value = playlist.also {
            logger.debug { "Playlist activated: ${playlist.id()}" }
        }
    }

}