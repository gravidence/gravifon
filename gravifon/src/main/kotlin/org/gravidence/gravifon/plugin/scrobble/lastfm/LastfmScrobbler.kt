package org.gravidence.gravifon.plugin.scrobble.lastfm

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.Settings
import org.gravidence.gravifon.configuration.readConfig
import org.gravidence.gravifon.configuration.writeConfig
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.gravidence.gravifon.orchestration.SettingsConsumer
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.plugin.scrobble.Scrobble
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.gravidence.lastfm4k.LastfmClient
import org.gravidence.lastfm4k.api.auth.Session
import org.gravidence.lastfm4k.api.track.IgnoreStatus
import org.gravidence.lastfm4k.api.track.Track
import org.gravidence.lastfm4k.exception.LastfmApiException
import org.gravidence.lastfm4k.exception.LastfmException
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

@Component
class LastfmScrobbler : Plugin(), SettingsConsumer {

    private val absoluteMinScrobbleDuration = 30.seconds
    private val absoluteEnoughScrobbleDuration = 4.minutes

    @Serializable
    data class LastfmScrobblerConfiguration(
        val session: Session? = null
    )

    private lateinit var appConfig: LastfmScrobblerConfiguration

    override lateinit var settings: Settings

    private lateinit var lastfmClient: LastfmClient
    private val scrobbleCache: MutableList<Scrobble> = mutableListOf()
    private var pendingScrobble: Scrobble? = null

    private val pluginConfig = Configuration()

    override fun consume(event: Event) {
        try {
            when (event) {
                is PubTrackStartEvent -> handle(event)
                is PubTrackFinishEvent -> handle(event)
            }
        } catch (exc: LastfmApiException) {
            logger.info(exc) { "Error response received from Last.fm service" }
        } catch (exc: LastfmException) {
            logger.info(exc) { "Error calling Last.fm service" }
        } catch (exc: Exception) {
            logger.error(exc) { "Exception caught while handling scrobble event" }
        }
    }

    override fun settingsReady(settings: Settings) {
        this.settings = settings

        appConfig = readConfig { LastfmScrobblerConfiguration() }

        pluginConfig.readConfiguration()

        lastfmClient = LastfmClient(
//            apiRoot = "http://ws.audioscrobbler.invalid/2.0/",
            apiKey = "3c3f2425f258b1bc2f7eddcd95194ef4",
            apiSecret = "a05c02f0b955060fc782f7a9270eeab6",
            session = appConfig.session
        )
    }

    override fun persistConfig() {
        writeConfig(appConfig)

        pluginConfig.writeConfiguration()
    }

    private fun handle(event: PubTrackStartEvent) {
        event.track.toLastfmTrack()?.let {
            pendingScrobble = Scrobble(track = event.track, startedAt = event.timestamp)

            val response = lastfmClient.trackApi.updateNowPlaying(it)

            if (response.result.scrobbleCorrectionSummary.status != IgnoreStatus.OK) {
                logger.info { "Scrobble will be ignored by service: reason=${response.result.scrobbleCorrectionSummary.status}" }
            }
        }
    }

    private fun handle(event: PubTrackFinishEvent) {
        event.track.toLastfmTrack()?.let {
            val pendingScrobbleFixed = pendingScrobble

            validateScrobbleEvent(pendingScrobbleFixed, event.track)

            if (pendingScrobbleFixed != null) {
                if (!scrobbleDurationMeetsRequirements(event)) {
                    logger.info { "Last.fm scrobbling criteria not met: trackLength=${event.track.getLength()}, scrobbleDuration=${event.duration}" }
                } else {
                    completePendingScrobble(pendingScrobbleFixed, event)

                    val candidateScrobbles = scrobbleCache.take(50)

                    val response = lastfmClient.trackApi.scrobble(candidateScrobbles.map { it.toLastfmScrobble() })

                    if (response.responseHolder.summary.ignored > 0) {
                        logger.info { "${response.responseHolder.summary.ignored} scrobbles were ignored by service" }
                    }

                    scrobbleCache.removeAll(candidateScrobbles)
                }
            }
        }
    }

    /**
     * Fulfill [pendingScrobble] with final details from [trackFinishEvent] and add it to [scrobbleCache].
     */
    private fun completePendingScrobble(pendingScrobble: Scrobble, trackFinishEvent: PubTrackFinishEvent) {
        pendingScrobble.duration = trackFinishEvent.duration
        pendingScrobble.finishedAt = trackFinishEvent.timestamp

        scrobbleCache += pendingScrobble
    }

    /**
     * Validate scrobble event for various inconsistencies. None of them should be the case in real life, but could be result of a bug.
     */
    private fun validateScrobbleEvent(pendingScrobble: Scrobble?, finishedTrack: VirtualTrack) {
        if (pendingScrobble == null) {
            logger.error { "There's no pending scrobble to complete and send" }
        } else {
            if (pendingScrobble.track != finishedTrack) {
                logger.error { "Just finished track doesn't match pending scrobble: $finishedTrack vs ${pendingScrobble.track}" }
            }
            if (pendingScrobble.duration != null) {
                logger.error { "Pending scrobble is already finalized: ${pendingScrobble.track}" }
            }
        }
    }

    /**
     * Verify that scrobble meets Last.fm requirements regarding duration.
     * Reference: [https://www.last.fm/api/scrobbling#when-is-a-scrobble-a-scrobble].
     */
    private fun scrobbleDurationMeetsRequirements(scrobbleEvent: PubTrackFinishEvent): Boolean {
        val scrobbleDuration = scrobbleEvent.duration

        if (scrobbleDuration < absoluteMinScrobbleDuration) {
            return false
        }

        if (scrobbleDuration >= absoluteEnoughScrobbleDuration) {
            return true
        }

        val trackLength = scrobbleEvent.track.getLength()
        return trackLength != null && scrobbleDuration >= trackLength.div(2)
    }

    private fun VirtualTrack.toLastfmTrack(): Track? {
        // secure mandatory params
        val artist = getArtist()
        val title = getTitle()

        return if (artist == null || title == null) {
            logger.info { "Track metadata doesn't meet requirements: $this" }
            null
        } else {
            Track(
                artist = artist,
                track = title,
                album = getAlbum(),
                albumArtist = getAlbumArtist(),
                duration = getLength()?.inWholeSeconds,
            )
        }
    }

    private fun Scrobble.toLastfmScrobble(): org.gravidence.lastfm4k.api.track.Scrobble {
        return org.gravidence.lastfm4k.api.track.Scrobble(
            track = track.toLastfmTrack()!!, // track is definitely not null if scrobble created already
            timestamp = startedAt.epochSeconds
        )
    }

    inner class Configuration {

        private val pluginDir: Path = pluginConfigHomeDir.resolve("lastfm-scrobbler")
        private val scrobbleCacheFile: Path = pluginDir.resolve("cache")

        init {
            pluginDir.createDirectories()
        }

        fun readConfiguration() {
            logger.debug { "Read scrobble cache from $scrobbleCacheFile" }

            try {
                if (scrobbleCacheFile.exists()) {
                    scrobbleCache.addAll(gravifonSerializer.decodeFromString(Files.readString(scrobbleCacheFile))).also {
                        logger.trace { "Scrobble cache loaded: $it" }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to read scrobble cache from $scrobbleCacheFile" }
            }
        }

        fun writeConfiguration() {
            logger.debug { "Write scrobble cache to $scrobbleCacheFile" }

            try {
                val playlistAsString = gravifonSerializer.encodeToString(scrobbleCache).also {
                    logger.trace { "Scrobble cache to be persisted: $it" }
                }
                Files.writeString(
                    scrobbleCacheFile,
                    playlistAsString,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                )
            } catch (e: Exception) {
                logger.error(e) { "Failed to write scrobble cache to $scrobbleCacheFile" }
            }
        }

    }

}