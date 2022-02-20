package org.gravidence.gravifon.plugin.library

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.track.compare.VirtualTrackSelectors
import org.gravidence.gravifon.orchestration.marker.Playable
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.gravidence.gravifon.plugin.Plugin
import org.springframework.stereotype.Component
import java.util.*

@Component
class Library(
    override val configurationManager: ConfigurationManager,
    override val playlistManager: PlaylistManager,
    val libraryStorage: LibraryStorage
) : Plugin, Playable {

    override val pluginDisplayName: String = "Library"
    override val pluginDescription: String = "Library v0.1"

    override val playlist: Playlist

    override val componentConfiguration: LibraryComponentConfiguration = readComponentConfiguration {
        LibraryComponentConfiguration(playlistId = UUID.randomUUID().toString())
    }

    init {
        playlist = playlistManager.getPlaylist(componentConfiguration.playlistId)
            ?: DynamicPlaylist(
                id = componentConfiguration.playlistId,
                ownerName = pluginDisplayName,
                displayName = componentConfiguration.playlistId
            ).also { playlistManager.addPlaylist(it) }
    }

    @Serializable
    data class LibraryComponentConfiguration(
        val playlistId: String,
        val queryHistory: MutableList<String> = mutableListOf(),
        val queryHistorySizeLimit: Int = 10,
        val sortOrder: MutableList<VirtualTrackSelectors> = mutableListOf()
    ) : ComponentConfiguration

    @Composable
    override fun composeSettings() {
        Text("TBD")
    }

}