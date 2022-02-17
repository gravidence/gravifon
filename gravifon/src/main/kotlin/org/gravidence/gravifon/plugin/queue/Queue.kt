package org.gravidence.gravifon.plugin.queue

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.configuration.readConfig
import org.gravidence.gravifon.configuration.writeConfig
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.orchestration.PlaylistManagerConsumer
import org.gravidence.gravifon.orchestration.SettingsConsumer
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.ui.View
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class Queue :
    Plugin(pluginDisplayName = "Queue", pluginDescription = "Queue v0.1"), View,
    SettingsConsumer, PlaylistManagerConsumer {

    @Serializable
    data class QueueViewConfiguration(val playlistId: String)

    private lateinit var viewConfig: QueueViewConfiguration

    override lateinit var settings: Settings
    private lateinit var playlistManager: PlaylistManager

    override fun consume(event: Event) {
        when (event) {
        }
    }

    override fun settingsReady(settings: Settings) {
        this.settings = settings

        viewConfig = readConfig {
//            QueueViewConfiguration(playlistId = UUID.randomUUID().toString())
            QueueViewConfiguration(playlistId = "b743b278-413d-4d47-b673-b2a26e4bbdc4")
        }
    }

    override fun persistConfig() {
        writeConfig(viewConfig)
    }

    override fun playlistManagerReady(playlistManager: PlaylistManager) {
        this.playlistManager = playlistManager
    }

    override fun registerPlaylist() {
        if (playlistManager.getPlaylist(viewConfig.playlistId) == null) {
            // by below call, Queue playlist is also activated automatically by PlaylistManager
            playlistManager.addPlaylist(Queue(viewConfig.playlistId))
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

}