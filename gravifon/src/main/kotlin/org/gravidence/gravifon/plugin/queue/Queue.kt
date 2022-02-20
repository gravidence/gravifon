package org.gravidence.gravifon.plugin.queue

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.gravidence.gravifon.plugin.Plugin
import org.springframework.stereotype.Component
import java.util.*

@Component
class Queue(override val configurationManager: ConfigurationManager, private val playlistManager: PlaylistManager) :
    Plugin, Viewable {

    override val pluginDisplayName: String = "Queue"
    override val pluginDescription: String = "Queue v0.1"

    override val componentConfiguration: QueueComponentConfiguration

    init {
        componentConfiguration = readComponentConfiguration {
            QueueComponentConfiguration(playlistId = UUID.randomUUID().toString())
        }

        if (playlistManager.getPlaylist(componentConfiguration.playlistId) == null) {
            // by below call, Queue playlist is also activated automatically by PlaylistManager
            playlistManager.addPlaylist(
                Queue(
                    id = componentConfiguration.playlistId,
                    ownerName = pluginDisplayName,
                    displayName = componentConfiguration.playlistId
                )
            )
        }
    }

    @Composable
    override fun composeSettings() {
        Text("TBD")
    }

    override fun viewDisplayName(): String {
        return pluginDisplayName
    }

    @Composable
    override fun composeView() {
        Text("TBD")
    }

    @Serializable
    data class QueueComponentConfiguration(
        val playlistId: String
    ) : ComponentConfiguration

}