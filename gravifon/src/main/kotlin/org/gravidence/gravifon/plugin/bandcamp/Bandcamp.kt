@file:OptIn(ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.plugin.bandcamp

import androidx.compose.foundation.layout.*
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.track.StreamVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.plugin.bandcamp.model.BandcampItem
import org.gravidence.gravifon.plugin.bandcamp.model.bandcampSerializer
import org.gravidence.gravifon.plugin.bandcamp.model.enhanced
import org.gravidence.lastfm4k.misc.toLocalDateTime
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import java.util.*
import kotlin.system.measureTimeMillis
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

@Component
class Bandcamp(
    override val configurationManager: ConfigurationManager,
) : Plugin {

    override var pluginEnabled: Boolean
        get() = componentConfiguration.value.enabled
        set(value) { componentConfiguration.value = componentConfiguration.value.copy(enabled = value)}
    override val pluginDisplayName: String = "Bandcamp"
    override val pluginDescription: String = "Bandcamp v0.1"

    private fun fetchPage(url: String): Document {
        val document: Document
        measureTimeMillis {
            document = Jsoup.connect(url).get()
        }.also {
            logger.debug { "Fetched in ${it}ms" }
        }
        return document
    }

    private fun selectBandcampItem(document: Document): String? {
        return document.select("script[data-band-follow-info]").firstOrNull()?.attr("data-tralbum")
    }

    fun parsePage(url: String): List<VirtualTrack> {
        logger.info { "Process Bandcamp page: $url" }
        return try {
            val bandcampItem = selectBandcampItem(fetchPage(url))
            if (bandcampItem != null) {
                bandcampSerializer.decodeFromString<BandcampItem>(bandcampItem).also {
                    logger.debug { "Bandcamp item parsed: $it" }
                }.let {
                    if (componentConfiguration.value.useEnhancer) {
                        it.enhanced().also {
                            logger.debug { "Bandcamp item enhanced: $it" }
                        }
                    } else {
                        it
                    }
                }
                .toVirtualTracks()
            } else {
                logger.error { "Bandcamp item node not found" }
                listOf()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to process Bandcamp page: $url" }
            listOf()
        }
    }

    @Serializable
    data class BandcampComponentConfiguration(
        val enabled: Boolean = true,
        val playlistId: String,
        var useEnhancer: Boolean,
    ) : ComponentConfiguration

    override val componentConfiguration = mutableStateOf(
        readComponentConfiguration {
            BandcampComponentConfiguration(
                playlistId = UUID.randomUUID().toString(),
                useEnhancer = false,
            )
        }
    )

    inner class BandcampSettingsState {

        fun toggleUseEnhancer() {
            componentConfiguration.value = componentConfiguration.value.copy(useEnhancer = componentConfiguration.value.useEnhancer.not())
        }

    }

    @Composable
    fun rememberBandcampSettingsState(
    ) = remember {
        BandcampSettingsState()
    }

    @Composable
    override fun composeSettings() {
        val state = rememberBandcampSettingsState()

        Box(
            modifier = Modifier
                .widthIn(min = 400.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .onPointerEvent(
                                    eventType = PointerEventType.Release,
                                    onEvent = { state.toggleUseEnhancer() }
                                )
                        ) {
                            Checkbox(
                                checked = componentConfiguration.value.useEnhancer,
                                onCheckedChange = {  }
                            )
                            Text("Use metadata enhancer")
                        }
                    }
                }
            }
        }
    }

}

fun BandcampItem.toVirtualTracks(): List<VirtualTrack> {
    return tracks.map { bandcampTrack ->
        StreamVirtualTrack(
            sourceUrl = this.url,
            streamUrl = bandcampTrack.file.mp3128,
            headers = Headers(length = bandcampTrack.duration.seconds)
        ).apply {
            setArtist(bandcampTrack.artist ?: albumArtist)
            setTitle(bandcampTrack.title)
            setAlbum(resolveAlbum())
            setDate(albumReleaseDate.toLocalDateTime().date.toString())
            setAlbumArtist(albumArtist)
            setTrack(bandcampTrack.tracknum.toString())
            setTrackTotal(tracks.size.toString())
        }
    }
}