package org.gravidence.gravifon.plugin.library

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.track.compare.VirtualTrackSelectors
import org.gravidence.gravifon.plugin.Plugin
import org.springframework.stereotype.Component
import java.util.*

@Component
class Library(
    override val configurationManager: ConfigurationManager,
    val libraryStorage: LibraryStorage
) : Plugin {

    override val pluginDisplayName: String = "Library"
    override val pluginDescription: String = "Library v0.1"

    override val componentConfiguration = mutableStateOf(
        readComponentConfiguration {
            LibraryComponentConfiguration(playlistId = UUID.randomUUID().toString())
        }
    )

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