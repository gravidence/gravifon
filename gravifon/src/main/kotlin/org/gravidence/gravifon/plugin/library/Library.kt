package org.gravidence.gravifon.plugin.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.configuration.FileStorage
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.domain.track.compare.VirtualTrackSelectors
import org.gravidence.gravifon.domain.track.virtualTrackComparator
import org.gravidence.gravifon.orchestration.marker.Configurable
import org.gravidence.gravifon.orchestration.marker.Playable
import org.gravidence.gravifon.orchestration.marker.Stateful
import org.gravidence.gravifon.orchestration.marker.Viewable
import org.gravidence.gravifon.playlist.DynamicPlaylist
import org.gravidence.gravifon.playlist.Playlist
import org.gravidence.gravifon.playlist.item.PlaylistItem
import org.gravidence.gravifon.playlist.item.TrackPlaylistItem
import org.gravidence.gravifon.playlist.manage.PlaylistManager
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.query.TrackQueryParser
import org.gravidence.gravifon.ui.PlaylistComposable
import org.gravidence.gravifon.ui.rememberPlaylistState
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.springframework.stereotype.Component
import org.springframework.util.Base64Utils
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.streams.toList

private val logger = KotlinLogging.logger {}

@Component
class Library(override val configurationManager: ConfigurationManager, override val playlistManager: PlaylistManager) :
    Plugin(pluginDisplayName = "Library", pluginDescription = "Library v0.1"),
    Viewable, Playable, Configurable, Stateful {

    private val roots: MutableList<Root> = ArrayList()

    override val playlist: Playlist

    override val componentConfiguration: LibraryConfiguration
    override val fileStorage: FileStorage = LibraryFileStorage()

    init {
        componentConfiguration = readComponentConfiguration {
            LibraryConfiguration(playlistId = UUID.randomUUID().toString())
        }

        fileStorage.read()

        playlist = playlistManager.getPlaylist(componentConfiguration.playlistId)
            ?: DynamicPlaylist(
                id = componentConfiguration.playlistId,
                ownerName = pluginDisplayName,
                displayName = componentConfiguration.playlistId
            ).also { playlistManager.addPlaylist(it) }

        logger.info { "Initialize library (${roots.size} roots configured)" }

        GravifonContext.scopeIO.launch {
            this@Library.roots
                .filter { it.scanOnInit }
                .forEach { it.scan() }
        }
    }

    @Synchronized
    fun getRoots(): List<Root> {
        return roots.toList()
    }

    @Synchronized
    fun addRoot(root: Root) {
        if (roots.none { it.rootDir == root.rootDir }) {
            roots.add(root).also {
                logger.info { "Register library root: ${root.rootDir}" }
            }
        }
    }

    @Synchronized
    fun allTracks(): List<VirtualTrack> {
        return roots
            .flatMap { root -> root.tracks }
            .toList()
    }

    @Composable
    override fun composeSettings() {
        Text("TBD")
    }

    inner class LibraryViewState(
        val query: MutableState<String>,
        val playlistItems: MutableState<List<PlaylistItem>>,
        val playlist: Playlist
    ) {

        fun onQueryChange(changed: String) {
            query.value = changed
            if (TrackQueryParser.validate(changed)) {
                val selection = TrackQueryParser.execute(changed, allTracks())
                    // TODO sortOrder should be part of view state
                    .sortedWith(virtualTrackComparator(componentConfiguration.sortOrder))
                    .map { TrackPlaylistItem(it) }
                playlist.replace(selection)
                playlistItems.value = playlist.items()

                if (componentConfiguration.queryHistory.size >= componentConfiguration.queryHistorySizeLimit) {
                    componentConfiguration.queryHistory.removeLast()
                }
                componentConfiguration.queryHistory.add(0, changed)
            }
        }

    }

    @Composable
    fun rememberLibraryViewState(
        query: MutableState<String> = mutableStateOf(""),
        playlistItems: MutableState<List<PlaylistItem>> = mutableStateOf(listOf()),
        playlist: Playlist
    ) = remember(query, playlistItems) { LibraryViewState(query, playlistItems, playlist) }

    override fun viewDisplayName(): String {
        return pluginDisplayName
    }

    @Composable
    override fun composeView() {
        val libraryViewState = rememberLibraryViewState(
            query = mutableStateOf(componentConfiguration.queryHistory.firstOrNull() ?: ""),
            playlistItems = mutableStateOf(playlist.items()),
            playlist = playlist
        )
        val playlistState = rememberPlaylistState(
            playlistItems = libraryViewState.playlistItems,
            playlist = playlist
        )

        Box(
            modifier = Modifier
                .padding(5.dp)
        ) {
            Column {
                Row {
                    QueryBar(libraryViewState)
                }
                Divider(color = Color.Transparent, thickness = 2.dp)
                Row {
                    PlaylistComposable(playlistState)
                }
            }
        }
    }

    @Composable
    fun QueryBar(libraryViewState: LibraryViewState) {
        Box(
            modifier = Modifier
                .height(50.dp)
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                .background(color = Color.LightGray, shape = RoundedCornerShape(5.dp))
        ) {
            BasicTextField(
                value = libraryViewState.query.value,
                singleLine = true,
                onValueChange = { libraryViewState.onQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .align(Alignment.CenterStart)
            )
        }
    }

    @Serializable
    data class LibraryConfiguration(
        val playlistId: String,
        val queryHistory: MutableList<String> = mutableListOf(),
        val queryHistorySizeLimit: Int = 10,
        val sortOrder: MutableList<VirtualTrackSelectors> = mutableListOf()
    ) : ComponentConfiguration

    inner class LibraryFileStorage : FileStorage(storageDir = pluginConfigHomeDir.resolve("library")) {

        override fun read() {
            val libraryRootConfigFiles = try {
                Files.list(storageDir).toList()
            } catch (e: Exception) {
                listOf()
            }.also {
                logger.info { "Discovered library root configuration files: $it" }
            }

            libraryRootConfigFiles.map { libraryRootConfigFile ->
                logger.debug { "Read library root configuration from $libraryRootConfigFile" }

                try {
                    addRoot(gravifonSerializer.decodeFromString(Files.readString(libraryRootConfigFile))).also {
                        logger.trace { "Library root configuration loaded: $it" }
                    }
                } catch (e: Exception) {
                    logger.error(e) { "Failed to read library root configuration from $libraryRootConfigFile" }
                }
            }
        }

        override fun write() {
            getRoots().forEach {
                val rootId = encode(it.rootDir)
                val libraryRootConfigAsString = gravifonSerializer.encodeToString(it).also {
                    logger.trace { "Library root ($rootId) configuration to be persisted: $it" }
                }
                val rootConfigFile = Path.of(storageDir.toString(), rootId).also {
                    logger.debug { "Write library root configuration to $it" }
                }
                try {
                    Files.writeString(
                        rootConfigFile,
                        libraryRootConfigAsString,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                    )
                } catch (e: Exception) {
                    logger.error(e) { "Failed to write library root configuration to $rootConfigFile" }
                }
            }
        }

        private fun encode(originalPath: String): String {
            return Base64Utils.encodeToUrlSafeString(originalPath.encodeToByteArray())
        }

    }

}