package org.gravidence.gravifon.plugin.scrobble.lastfm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
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
import org.gravidence.gravifon.ui.tooltip
import org.gravidence.gravifon.util.serialization.gravifonSerializer
import org.gravidence.gravifon.plugin.View
import org.gravidence.lastfm4k.LastfmClient
import org.gravidence.lastfm4k.api.auth.Session
import org.gravidence.lastfm4k.api.auth.Token
import org.gravidence.lastfm4k.api.track.IgnoreStatus
import org.gravidence.lastfm4k.api.track.Track
import org.gravidence.lastfm4k.exception.LastfmApiException
import org.gravidence.lastfm4k.exception.LastfmException
import org.gravidence.lastfm4k.exception.LastfmNetworkException
import org.http4k.core.Uri
import org.springframework.stereotype.Component
import java.awt.Desktop
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * Plugin to send scrobbles to Last.fm service.
 * Reference: [https://www.last.fm/api/scrobbling].
 */
@Component
class LastfmScrobbler : Plugin(title = "Last.fm Scrobbler", description = "Last.fm Scrobbler v0.1"), View, SettingsConsumer {

    private val absoluteMinScrobbleDuration = 30.seconds
    private val absoluteEnoughScrobbleDuration = 4.minutes

    @Serializable
    data class LastfmScrobblerConfiguration(
        var session: Session? = null
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

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun composeSettings() {
        var session: Session? by remember { mutableStateOf(appConfig.session) }
        var authorization: Pair<Token, Uri>? by remember { mutableStateOf(null) }
        var error: String? by remember { mutableStateOf(null) }

        Box(
            modifier = Modifier
                .widthIn(min = 400.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    TooltipArea(
                        delayMillis = 600,
                        tooltip = { tooltip("Session key used by Gravifon to access your Last.fm account") }
                    ) {
                        Text("Session Key:")
                    }
                    BasicTextField(
                        value = session?.key ?: "",
                        singleLine = true,
                        readOnly = true,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                            .padding(5.dp)
                    )
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 30.dp, vertical = 5.dp)
                )
                if (session == null) {
                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = """
                            Gravifon isn't authorized to access your Last.fm account. Few steps are required to sort it out.
                            1. Request token
                            2. Allow Gravifon to access your Last.fm account. For that, please open Authorization URL in your browser and follow instructions. You need to be logged-in to complete this step.
                            3. Request session
                            """.trimIndent(),
                            fontStyle = FontStyle.Italic
                        )
                    }
                    error?.let {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(
                                text = it,
                                color = Color.Red,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        TooltipArea(
                            delayMillis = 600,
                            tooltip = { tooltip("Please this URL to authorize Gravifon to access your Last.fm account") }
                        ) {
                            Text("Authorization URL:")
                        }
                        BasicTextField(
                            value = authorization?.second?.toString() ?: "",
                            singleLine = true,
                            readOnly = true,
                            onValueChange = {},
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(5.dp))
                                .padding(5.dp)
                        )
                        Button(
                            enabled = authorization != null,
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                Desktop.getDesktop().browse(URI(authorization?.second.toString()))
                            },
//                            modifier = Modifier
//                                .size(30.dp)
                        ) {
//                            Text("⏵")
                            Text(">>")
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        enabled = session == null && authorization == null,
                        onClick = {
                            error = handleLastfmException {
                                authorization = lastfmClient.authorizeStep1()
                            }
                        }
                    ) {
                        Text("Request Token")
                    }
                    if (session == null) {
                        Button(
                            enabled = authorization != null,
                            onClick = {
                                authorization?.let {
                                    error = handleLastfmException {
                                        appConfig.session = lastfmClient.authorizeStep2(token = it.first)
                                        session = appConfig.session
                                    }
                                }
                            }
                        ) {
                            Text("Request Session")
                        }

                    } else {
                        Button(
                            onClick = {
                                // TODO too many assignments wrt same thing...
                                appConfig.session = null
                                lastfmClient.session(null)
                                session = null
                            }
                        ) {
                            Text("Clear Session")
                        }
                    }
                }
            }
        }
    }

    private fun handleLastfmException(block: () -> Unit): String? {
        return try {
            block()
            null
        } catch (exc: LastfmApiException) {
            "Error received from Last.fm service: ${exc.message}"
        } catch (exc: LastfmNetworkException) {
            "Failed to call Last.fm service: HTTP ${exc.response.status.code}"
        } catch (exc: LastfmException) {
            "Failed to call Last.fm service, please see logs for details"
        }
    }

    @Composable
    override fun composeView() {
        TODO("Not yet implemented")
    }

}