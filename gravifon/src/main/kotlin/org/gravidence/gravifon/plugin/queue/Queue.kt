package org.gravidence.gravifon.plugin.queue

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
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

    override val componentConfiguration = mutableStateOf(
        readComponentConfiguration {
            QueueComponentConfiguration(playlistId = UUID.randomUUID().toString())
        }
    )

    init {
        val cc = componentConfiguration.value

        if (playlistManager.getPlaylist(cc.playlistId) == null) {
            // by below call, Queue playlist is also activated automatically by PlaylistManager
            playlistManager.addPlaylist(
                Queue(
                    id = cc.playlistId,
                    ownerName = pluginDisplayName,
                    displayName = cc.playlistId
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