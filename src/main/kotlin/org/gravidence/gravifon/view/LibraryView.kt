package org.gravidence.gravifon.view

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.library.Library
import org.gravidence.gravifon.orchestration.PlaylistManagerConsumer
import org.gravidence.gravifon.orchestration.SettingsConsumer
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LibraryView : View(), SettingsConsumer, PlaylistManagerConsumer {

    @Serializable
    data class LibraryViewConfiguration(val playlistId: String)

    private lateinit var viewConfig: LibraryViewConfiguration

    override lateinit var settings: Settings
    private lateinit var playlistManager: PlaylistManager
    private lateinit var library: Library

    override fun consume(event: Event) {
        when (event) {
        }
    }

    override fun settingsReady(settings: Settings) {
        this.settings = settings

        val viewConfigAsString = readViewConfig()
        viewConfig = if (viewConfigAsString == null) {
            logger.debug { "Create new configuration" }
//            LibraryViewConfiguration(playlistId = UUID.randomUUID().toString())
            LibraryViewConfiguration(playlistId = "64ce227a-261e-4d52-ac28-83de7a08b6d8")
        } else {
            logger.debug { "Use configuration: $viewConfigAsString" }
            Json.decodeFromString(viewConfigAsString)
        }
    }

    override fun persistConfig() {
        val viewConfigAsString = Json.encodeToString(viewConfig).also {
            logger.debug { "Persist configuration: $it" }
        }
        writeViewConfig(viewConfigAsString)    }

    override fun playlistManagerReady(playlistManager: PlaylistManager) {
        this.playlistManager = playlistManager
    }

    override fun registerPlaylist() {
        if (playlistManager.getPlaylist(viewConfig.playlistId) == null) {
            playlistManager.addPlaylist(DynamicPlaylist(viewConfig.playlistId))
        }
    }

}