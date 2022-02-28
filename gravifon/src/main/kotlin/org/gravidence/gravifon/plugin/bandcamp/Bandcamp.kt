package org.gravidence.gravifon.plugin.bandcamp

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import org.gravidence.gravifon.plugin.bandcamp.model.BandcampItemType
import org.gravidence.gravifon.plugin.bandcamp.model.bandcampSerializer
import org.gravidence.lastfm4k.misc.toLocalDateTime
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

@Component
class Bandcamp(
    override val configurationManager: ConfigurationManager,
) : Plugin {

    override val pluginDisplayName: String = "Bandcamp"
    override val pluginDescription: String = "Bandcamp v0.1"

    fun parsePage(url: String): List<VirtualTrack> {
        logger.debug { "Process Bandcamp page: $url" }
        return try {
            val document = Jsoup.connect(url).get()
            val node = document.select("script[data-band-follow-info]").firstOrNull()?.attr("data-tralbum")
            if (node != null) {
                bandcampSerializer.decodeFromString<BandcampItem>(node).also {
                    logger.debug { "Bandcamp item parsed: $it" }
                }
                .enhanced().also {
                    logger.debug { "Bandcamp item enhanced: $it" }
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
        val playlistId: String,
    ) : ComponentConfiguration

    override val componentConfiguration: BandcampComponentConfiguration = readComponentConfiguration {
        BandcampComponentConfiguration(playlistId = UUID.randomUUID().toString())
    }

    @Composable
    override fun composeSettings() {
        Text("TBD")
    }

}

fun BandcampItem.enhanced(): BandcampItem {
    return when (type) {
        BandcampItemType.ALBUM -> {
            this
        }
        BandcampItemType.TRACK -> {
            this
        }
    }
}

fun BandcampItem.toVirtualTracks(): List<VirtualTrack> {
    return tracks.map { bandcampTrack ->
        StreamVirtualTrack(
            url = bandcampTrack.file.mp3128,
            headers = Headers(length = bandcampTrack.duration.seconds)
        ).apply {
            setArtist(bandcampTrack.artist ?: resolveAlbumArtist())
            setTitle(bandcampTrack.title)
            setAlbum(resolveAlbum())
            setDate(resolveAlbumReleaseDate().toLocalDateTime().date.toString())
            setAlbumArtist(resolveAlbumArtist())
            setTrack(bandcampTrack.tracknum.toString())
        }
    }
}