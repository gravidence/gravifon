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
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.domain.notification.NotificationLifespan
import org.gravidence.gravifon.domain.notification.NotificationType
import org.gravidence.gravifon.domain.track.StreamVirtualTrack
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.EventBus
import org.gravidence.gravifon.event.application.PushInnerNotificationEvent
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.plugin.bandcamp.model.BandcampItem
import org.gravidence.gravifon.plugin.bandcamp.model.bandcampSerializer
import org.gravidence.gravifon.plugin.bandcamp.model.enhanced
import org.gravidence.gravifon.plugin.bandcamp.model.expiresAfter
import org.gravidence.lastfm4k.misc.toLocalDateTime
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.springframework.stereotype.Component
import java.net.URI
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

    private val pageCache: MutableMap<String, Document> = mutableMapOf()

    private fun fetchPage(url: String): Document {
        val document: Document
        measureTimeMillis {
            document = pageCache.getOrElse(url) { Jsoup.connect(url).get() }
        }.also {
            logger.debug { "Fetched in ${it}ms" }
        }
        return document
    }

    private fun extractAlbumOrTrackJson(document: Document): String? {
        return document.select("script[data-band-follow-info]")
            .map { it.attr("data-tralbum") }
            .firstOrNull()
    }

    private fun extractAlbumUrls(document: Document, discographyAbsoluteUrl: String): List<String>? {
        val albumUrls = document.select("ol#music-grid li a[href]")
        return if (albumUrls.isNotEmpty()) {
            val discographyUri = URI(discographyAbsoluteUrl)
            albumUrls
                .map { it.attr("href") }
                .map {
                    val releaseUri = URI(it)
                    if (releaseUri.isAbsolute) {
                        // links to artist dedicated pages are absolute
                        it
                    } else {
                        // otherwise, refer to label's relative page
                        discographyUri.resolve(releaseUri).toString()
                    }
                }
        } else {
            null
        }
    }

    private fun processAlbumOrTrackJson(json: String): List<StreamVirtualTrack> {
        return bandcampSerializer.decodeFromString<BandcampItem>(json).also {
            logger.debug { "Bandcamp item parsed: $it" }
        }.let {
            if (componentConfiguration.value.useEnhancer) {
                it.enhanced().also {
                    logger.debug { "Bandcamp item enhanced: $it" }
                }
            } else {
                it
            }
        }.toStreamVirtualTracks()
    }

    /**
     * Page cache is used to avoid fetching same page twice. This method performs a clean-up, should be called upon processing completion.
     */
    fun clearPageCache() {
        pageCache.clear()
    }

    /**
     * Recognize whether supplied [url] is a Bandcamp discography, album or track page.
     * @return list of absolute links to resulting Bandcamp pages, if any
     */
    fun resolveBandcampPages(url: String): List<String> {
        logger.info { "Resolve Bandcamp pages at URL: $url" }
        return try {
            val document = pageCache.getOrPut(url) { fetchPage(url) }
            extractAlbumUrls(document, url) ?: listOf(url)
        } catch (e: Exception) {
            val message = "Failed to process URL: $url"
            logger.error(e) { message }
            EventBus.publish(
                PushInnerNotificationEvent(
                    Notification(
                        message = message,
                        type = NotificationType.ERROR,
                        lifespan = NotificationLifespan.MEDIUM
                    )
                )
            )
            listOf()
        }.also {
            logger.debug { "Bandcamp pages resolved: ${it.size}" }
        }
    }

    /**
     * Parse Bandcamp album or track page.
     * @return list of stream tracks
     */
    fun parseBandcampPage(pageUrl: String): List<StreamVirtualTrack> {
        logger.info { "Process page: $pageUrl" }
        return try {
            val json = extractAlbumOrTrackJson(fetchPage(pageUrl))
            if (json != null) {
                processAlbumOrTrackJson(json)
            } else {
                logger.error { "Bandcamp item node not found" }
                EventBus.publish(
                    PushInnerNotificationEvent(
                        Notification(
                            message = "Unable to process: not a Bandcamp page",
                            type = NotificationType.REGULAR,
                            lifespan = NotificationLifespan.MEDIUM
                        )
                    )
                )
                listOf()
            }
        } catch (e: Exception) {
            val message = "Failed to process page: $pageUrl"
            logger.error(e) { message }
            EventBus.publish(
                PushInnerNotificationEvent(
                    Notification(
                        message = message,
                        type = NotificationType.ERROR,
                        lifespan = NotificationLifespan.MEDIUM
                    )
                )
            )
            listOf()
        }
    }

    /**
     * Select expired stream tracks.
     * @return list of expired tracks grouped by source Bandcamp page
     */
    fun selectExpiredTracks(tracks: List<VirtualTrack>): Map<String, List<StreamVirtualTrack>> {
        return tracks
            .filterIsInstance<StreamVirtualTrack>()
            .filter { stream -> stream.expiresAfter?.compareTo(Clock.System.now())?.let { it < 0 } ?: false }
            .groupBy { it.sourceUrl }.also {
                logger.debug { "Bandcamp pages to refresh: ${it.size}" }
            }
    }

    /**
     * Update expired stream tracks by re-querying their source Bandcamp page.
     */
    fun refreshExpiredTracks(pageUrl: String, streams: List<StreamVirtualTrack>) {
        logger.debug { "Refresh stream URLs from Bandcamp page: $pageUrl" }
        val freshStreams = parseBandcampPage(pageUrl)

        streams.forEach { stream ->
            stream.getTrack()?.toIntOrNull()?.let { trackNumber ->
                freshStreams.elementAtOrNull(trackNumber - 1)?.let { freshTrack ->
                    stream.streamUrl = freshTrack.streamUrl
                }
            }
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

fun BandcampItem.toStreamVirtualTracks(): List<StreamVirtualTrack> {
    return tracks
        .filter { it.file != null }
        .map { bandcampTrack ->
            StreamVirtualTrack(
                sourceUrl = this.url,
                streamUrl = bandcampTrack.file!!.mp3128,
                expiresAfter = bandcampTrack.file.expiresAfter(),
                headers = Headers(length = bandcampTrack.duration.seconds)
            ).apply {
                setArtist(bandcampTrack.artist ?: albumArtist)
                setTitle(bandcampTrack.title)
                setAlbum(resolveAlbum())
                albumReleaseDate?.let { setDate(it.toLocalDateTime().date.toString()) }
                setAlbumArtist(albumArtist)
                bandcampTrack.tracknum?.let { setTrack(it.toString()) }
                setTrackTotal(tracks.size.toString())
            }
        }
}