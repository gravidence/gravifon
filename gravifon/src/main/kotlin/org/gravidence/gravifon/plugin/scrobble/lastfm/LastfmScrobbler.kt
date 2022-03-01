package org.gravidence.gravifon.plugin.scrobble.lastfm

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.TooltipArea
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import mu.KotlinLogging
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.track.PubTrackFinishEvent
import org.gravidence.gravifon.event.track.PubTrackStartEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.plugin.scrobble.Scrobble
import org.gravidence.gravifon.ui.image.AppIcon
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.ui.tooltip
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
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * Plugin to send scrobbles to Last.fm service.
 * See [documentation](https://www.last.fm/api/scrobbling).
 */
@Component
class LastfmScrobbler(override val configurationManager: ConfigurationManager, val lastfmScrobblerStorage: LastfmScrobblerStorage) :
    Plugin, EventAware {

    override val pluginDisplayName: String = "Last.fm Scrobbler"
    override val pluginDescription: String = "Last.fm Scrobbler v0.1"

    private val absoluteMinScrobbleDuration = 30.seconds
    private val absoluteEnoughScrobbleDuration = 4.minutes

    private var pendingScrobble: Scrobble? = null

    override fun consume(event: Event) {
        when (event) {
            is PubTrackStartEvent -> handle(event)
            is PubTrackFinishEvent -> handle(event)
        }
    }

    private fun handle(event: PubTrackStartEvent) {
        event.track.toLastfmTrack()?.let {
            pendingScrobble = Scrobble(track = event.track, startedAt = event.timestamp)

            if (componentConfiguration.autoScrobble && componentConfiguration.sendNowPlaying) {
                sendNowPlaying(it)
            } else {
                logger.debug { "\"Now Playing\" notifications are disabled" }
            }
        }
    }

    private fun sendNowPlaying(track: Track) {
        handleLastfmException {
            val response = lastfmClient.trackApi.updateNowPlaying(track)

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

                    if (componentConfiguration.autoScrobble) {
                        scrobble()
                    } else {
                        logger.debug { "Auto scrobble is disabled. ${lastfmScrobblerStorage.scrobbleCache().size} scrobbles in cache" }
                    }
                }
            }
        }
    }

    fun scrobble() {
        handleLastfmException {
            while (lastfmScrobblerStorage.scrobbleCache().isNotEmpty()) {
                val scrobbleCache = lastfmScrobblerStorage.scrobbleCache()
                val candidateScrobbles = scrobbleCache.take(50).also {
                    logger.info { "Submitting ${it.size} out of ${scrobbleCache.size} scrobbles..." }
                }

                val response = lastfmClient.trackApi.scrobble(candidateScrobbles.map { it.toLastfmScrobble() })

                if (response.responseHolder.summary.ignored > 0) {
                    logger.info { "${response.responseHolder.summary.ignored} scrobbles were ignored by service" }
                }

                lastfmScrobblerStorage.removeFromScrobbleCache(candidateScrobbles)
            }
        }
    }

    /**
     * Fulfill [pendingScrobble] with final details from [trackFinishEvent] and add it to [lastfmScrobblerStorage].
     */
    private fun completePendingScrobble(pendingScrobble: Scrobble, trackFinishEvent: PubTrackFinishEvent) {
        pendingScrobble.duration = trackFinishEvent.duration
        pendingScrobble.finishedAt = trackFinishEvent.timestamp

        lastfmScrobblerStorage.appendToScrobbleCache(pendingScrobble)
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
     * See [documentation](https://www.last.fm/api/scrobbling#when-is-a-scrobble-a-scrobble).
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

    private fun handleLastfmException(block: () -> Unit): String? {
        return try {
            block()
            null
        } catch (e: LastfmApiException) {
            "Error received from Last.fm service: ${e.message}".also {
                logger.info(e) { it }
            }
        } catch (e: LastfmNetworkException) {
            "Failed to call Last.fm service: HTTP ${e.response.status.code}".also {
                logger.warn(e) { it }
            }
        } catch (e: LastfmException) {
            logger.error(e) { "Exception caught while handling Last.fm API request" }
            "Failed to call Last.fm service, please see logs for details"
        } finally {
            logger.debug { "${lastfmScrobblerStorage.scrobbleCache().size} scrobbles in cache" }
        }
    }

    @Serializable
    data class LastfmScrobblerComponentConfiguration(
        var session: Session? = null,
        var autoScrobble: Boolean = true,
        var sendNowPlaying: Boolean = true
    ) : ComponentConfiguration

    override val componentConfiguration: LastfmScrobblerComponentConfiguration = readComponentConfiguration {
        LastfmScrobblerComponentConfiguration()
    }

    val lastfmClient: LastfmClient = LastfmClient(
        apiKey = "3c3f2425f258b1bc2f7eddcd95194ef4",
        apiSecret = "a05c02f0b955060fc782f7a9270eeab6",
        session = componentConfiguration.session
    )

    inner class LastfmScrobblerSettingsState(
        val session: MutableState<Session?>,
        val autoScrobble: MutableState<Boolean>,
        val sendNowPlaying: MutableState<Boolean>,
    ) {

        fun updateSession(session: Session?) {
            this.session.value = session

            componentConfiguration.session = session
            lastfmClient.session(session)
        }

        fun updateAutoScrobble(autoScrobble: Boolean) {
            this.autoScrobble.value = autoScrobble

            componentConfiguration.autoScrobble = autoScrobble
        }

        fun toggleAutoScrobble() {
            updateAutoScrobble(!autoScrobble.value)
        }

        fun updateSendNowPlaying(sendNowPlaying: Boolean) {
            this.sendNowPlaying.value = sendNowPlaying

            componentConfiguration.sendNowPlaying = sendNowPlaying
        }

        fun toggleSendNowPlaying() {
            updateSendNowPlaying(!sendNowPlaying.value)
        }

    }

    @Composable
    fun rememberLastfmScrobblerSettingsState(
        session: MutableState<Session?>,
        autoScrobble: MutableState<Boolean>,
        sendNowPlaying: MutableState<Boolean>,
    ) = remember(session, autoScrobble, sendNowPlaying) { LastfmScrobblerSettingsState(session, autoScrobble, sendNowPlaying) }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
    @Composable
    override fun composeSettings() {
        val state: LastfmScrobblerSettingsState = rememberLastfmScrobblerSettingsState(
            session = mutableStateOf(componentConfiguration.session),
            autoScrobble = mutableStateOf(componentConfiguration.autoScrobble),
            sendNowPlaying = mutableStateOf(componentConfiguration.sendNowPlaying),
        )
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
                        value = state.session.value?.key ?: "",
                        singleLine = true,
                        readOnly = true,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(width = 1.dp, color = Color.Black, shape = gShape)
                            .padding(5.dp)
                    )
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 30.dp, vertical = 5.dp)
                )
                if (state.session.value == null) {
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
                                .border(width = 1.dp, color = Color.Black, shape = gShape)
                                .padding(5.dp)
                        )
                        Button(
                            enabled = authorization != null,
                            contentPadding = PaddingValues(0.dp),
                            onClick = {
                                Desktop.getDesktop().browse(URI(authorization?.second.toString()))
                            },
                            modifier = Modifier
                                .size(30.dp)
                        ) {
                            AppIcon(path = "icons8-upload-24.png", modifier = Modifier.size(16.dp))
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
                        enabled = state.session.value == null && authorization == null,
                        onClick = {
                            error = handleLastfmException {
                                authorization = lastfmClient.authorizeStep1()
                            }
                        }
                    ) {
                        Text("Request Token")
                    }
                    if (state.session.value == null) {
                        Button(
                            enabled = authorization != null,
                            onClick = {
                                authorization?.let {
                                    error = handleLastfmException {
                                        state.updateSession(lastfmClient.authorizeStep2(token = it.first))
                                    }
                                }
                            }
                        ) {
                            Text("Request Session")
                        }

                    } else {
                        Button(
                            onClick = {
                                state.updateSession(null)
                            }
                        ) {
                            Text("Clear Session")
                        }
                    }
                }
                Divider(
                    thickness = 2.dp,
                    modifier = Modifier
                        .padding(horizontal = 30.dp, vertical = 5.dp)
                )
                Row {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .onPointerEvent(
                                    eventType = PointerEventType.Release,
                                    onEvent = {
                                        state.toggleAutoScrobble()
                                    }
                                )
                        ) {
                            Checkbox(
                                checked = state.autoScrobble.value,
                                onCheckedChange = {  }
                            )
                            Text("Scrobble automatically")
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .onPointerEvent(
                                    eventType = PointerEventType.Release,
                                    onEvent = {
                                        if (state.autoScrobble.value) {
                                            state.toggleSendNowPlaying()
                                        }
                                    }
                                )
                        ) {
                            Checkbox(
                                enabled = state.autoScrobble.value,
                                checked = state.sendNowPlaying.value,
                                onCheckedChange = {  }
                            )
                            Text("Send \"Now Playing\" notifications")
                        }
                    }
                }
            }
        }
    }

}