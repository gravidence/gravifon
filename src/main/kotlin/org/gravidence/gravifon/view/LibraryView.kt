package org.gravidence.gravifon.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.playlist.SubPlaylistActivateRegularPlaylistEvent
import org.gravidence.gravifon.orchestration.LibraryConsumer
import org.gravidence.gravifon.orchestration.PlaylistManagerConsumer
import org.gravidence.gravifon.orchestration.SettingsConsumer
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.gravidence.gravifon.plugin.library.Library
import org.gravidence.gravifon.query.TrackQueryParser
import org.gravidence.gravifon.ui.rememberPlaylistState
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class LibraryView : View(), SettingsConsumer, PlaylistManagerConsumer, LibraryConsumer {

    @Serializable
    data class LibraryViewConfiguration(val playlistId: String)

    private lateinit var viewConfig: LibraryViewConfiguration

    override lateinit var settings: Settings
    private lateinit var playlistManager: PlaylistManager
    private lateinit var library: Library

    private lateinit var playlist: Playlist

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
        writeViewConfig(viewConfigAsString)
    }

    override fun playlistManagerReady(playlistManager: PlaylistManager) {
        this.playlistManager = playlistManager
    }

    override fun registerPlaylist() {
        playlist = playlistManager.getPlaylist(viewConfig.playlistId) ?: DynamicPlaylist(viewConfig.playlistId)
        playlistManager.addPlaylist(playlist)

        EventBus.publish(SubPlaylistActivateRegularPlaylistEvent(viewConfig.playlistId))
    }

    override fun libraryReady(library: Library) {
        this.library = library
    }

    inner class LibraryViewState(val query: MutableState<String>, val playlistItems: MutableState<List<PlaylistItem>>, val playlist: Playlist) {

        fun onQueryChange(changed: String) {
            query.value = changed
            if (TrackQueryParser.validate(changed)) {
                playlist.replace(TrackQueryParser.execute(changed, library.allTracks()).map { TrackPlaylistItem(it) })
                playlistItems.value = playlist.items()
            }
        }

    }

    @Composable
    fun rememberLibraryViewState(
        query: MutableState<String> = mutableStateOf(""),
        playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(listOf()),
        playlist: Playlist
    ) = remember(query, playlistItems) { LibraryViewState(query, playlistItems, playlist) }

    @Composable
    override fun compose() {
        val libraryViewState = rememberLibraryViewState(playlist = playlist)
        val playlistState = rememberPlaylistState(libraryViewState.playlistItems, playlist)

        Column {
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                QueryBar(libraryViewState)
            }
            Divider(thickness = 3.dp)
            Row {
                org.gravidence.gravifon.ui.PlaylistComposable(playlistState)
            }
        }
    }

    @Composable
    fun QueryBar(libraryViewState: LibraryViewState) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.LightGray, shape = RoundedCornerShape(10.dp))
        ) {
            BasicTextField(
                value = libraryViewState.query.value,
                onValueChange = {
                    libraryViewState.onQueryChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .align(Alignment.CenterStart),
                singleLine = true
            )
        }
    }

}