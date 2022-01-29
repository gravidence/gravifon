package org.gravidence.gravifon.view

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.orchestration.PlaylistManagerConsumer
import org.gravidence.gravifon.orchestration.SettingsConsumer
import org.gravidence.gravifon.playlist.Queue
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class QueueView : View(), SettingsConsumer, PlaylistManagerConsumer {

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

        val viewConfigAsString = readViewConfig()
        viewConfig = if (viewConfigAsString == null) {
            logger.debug { "Create new configuration" }
//            QueueViewConfiguration(playlistId = UUID.randomUUID().toString())
            QueueViewConfiguration(playlistId = "b743b278-413d-4d47-b673-b2a26e4bbdc4")
        } else {
            logger.debug { "Use configuration: $viewConfigAsString" }
            Json.decodeFromString(viewConfigAsString)
        }
    }

    override fun persistConfig() {
        val viewConfigAsString = Json.encodeToString(viewConfig).also {
            logger.debug { "Persist configuration: $it" }
        }
        writeViewConfig(viewConfigAsString)
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
    override fun compose() {
        TODO("Not yet implemented")
    }

}