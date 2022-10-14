package org.gravidence.gravifon.plugin.scrobble.lastfm

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import org.gravidence.gravifon.GravifonContext
import org.gravidence.gravifon.configuration.ComponentConfiguration
import org.gravidence.gravifon.configuration.ConfigurationManager
import org.gravidence.gravifon.domain.notification.Notification
import org.gravidence.gravifon.domain.notification.NotificationLifespan
import org.gravidence.gravifon.domain.notification.NotificationType
import org.gravidence.gravifon.domain.track.VirtualTrack
import org.gravidence.gravifon.event.Event
import org.gravidence.gravifon.event.application.PushInnerNotificationEvent
import org.gravidence.gravifon.event.track.TrackFinishedEvent
import org.gravidence.gravifon.event.track.TrackStartedEvent
import org.gravidence.gravifon.orchestration.marker.EventAware
import org.gravidence.gravifon.plugin.Plugin
import org.gravidence.gravifon.plugin.scrobble.Scrobble
import org.gravidence.gravifon.ui.TextTooltip
import org.gravidence.gravifon.ui.theme.gShape
import org.gravidence.gravifon.util.DesktopUtil
import org.gravidence.lastfm4k.LastfmClient
import org.gravidence.lastfm4k.api.auth.Session
import org.gravidence.lastfm4k.api.auth.Token
import org.gravidence.lastfm4k.api.track.IgnoreStatus
import org.gravidence.lastfm4k.api.track.Track
import org.gravidence.lastfm4k.api.track.TrackInfoResponse
import org.gravidence.lastfm4k.api.track.comply
import org.gravidence.lastfm4k.exception.LastfmApiException
import org.gravidence.lastfm4k.exception.LastfmException
import org.gravidence.lastfm4k.exception.LastfmNetworkException
import org.gravidence.lastfm4k.exception.LastfmSerializationException
import org.http4k.core.Uri
import org.springframework.stereotype.Component
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

    override var pluginEnabled: Boolean
        get() = componentConfiguration.value.enabled
        set(value) { componentConfiguration.value = componentConfiguration.value.copy(enabled = value)}
    override val pluginDisplayName: String = "Last.fm Scrobbler"
    override val pluginDescription: String = "Last.fm Scrobbler v0.1"

    private val absoluteMinScrobbleDuration = 30.seconds
    private val absoluteEnoughScrobbleDuration = 4.minutes

    private var pendingScrobble: Scrobble? = null

    override fun consume(event: Event) {
        if (pluginEnabled) {
            when (event) {
                is TrackStartedEvent -> handle(event)
                is TrackFinishedEvent -> handle(event)
            }
        }
    }

    private fun handle(event: TrackStartedEvent) {
        event.track.toLastfmTrack()?.let {
            pendingScrobble = Scrobble(track = event.track, startedAt = event.timestamp)

            if (componentConfiguration.value.autoScrobble && componentConfiguration.value.sendNowPlaying) {
                sendNowPlaying(it)
            } else {
                logger.debug { "\"Now Playing\" notifications are disabled" }
            }

            getTrackInfo(it)
        }
    }

    private fun sendNowPlaying(track: Track) {
        handleLastfmException {
            val response = lastfmClient.trackApi.updateNowPlaying(track)

            if (response.result.scrobbleCorrectionSummary.status != IgnoreStatus.OK) {
                logger.warn { "Scrobble will be ignored by service: ${response.result.scrobbleCorrectionSummary.status}" }
            }
        }
    }

    private fun getTrackInfo(track: Track) {
        handleLastfmException {
            val response = lastfmClient.trackApi.getInfo(track)

            val playcountExtraInfo = resolveUserPlaycount(track, response)?.let {
                publish(
                    PushInnerNotificationEvent(
                        Notification(
                            message = "You got $it scrobbles for \"${track.artist} - ${track.track}\"",
                            type = NotificationType.REGULAR,
                            lifespan = NotificationLifespan.LONG
                        )
                    )
                )

                "$it scrobbles"
            }

            val lovedExtraInfo = if (response.trackInfo.userLoved == true) "♥" else null

            GravifonContext.activeTrackExtraInfo.value += listOfNotNull(playcountExtraInfo, lovedExtraInfo)
                .joinToString(separator = ",", prefix = "Last.fm: ")
        }
    }

    private fun resolveUserPlaycount(track: Track, response: TrackInfoResponse): Long? {
        return response.trackInfo.userPlaycount
    }

    private fun handle(event: TrackFinishedEvent) {
        event.track.toLastfmTrack()?.let {
            val pendingScrobbleFixed = pendingScrobble

            validateScrobbleEvent(pendingScrobbleFixed, event.track)

            if (pendingScrobbleFixed != null) {
                if (!scrobbleDurationMeetsRequirements(event)) {
                    logger.info { "Last.fm scrobbling criteria not met: trackLength=${event.track.getLength()}, scrobbleDuration=${event.duration}" }
                } else {
                    completePendingScrobble(pendingScrobbleFixed, event).also {
                        publish(
                            PushInnerNotificationEvent(
                                Notification(
                                    message = "Complete scrobble. ${lastfmScrobblerStorage.scrobbleCache().size} scrobbles in cache",
                                    type = NotificationType.MINOR,
                                    lifespan = NotificationLifespan.MEDIUM
                                )
                            )
                        )
                    }

                    if (componentConfiguration.value.autoScrobble) {
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
                    val message = "Submitting ${it.size} out of ${scrobbleCache.size} scrobbles..."
                    logger.info { message }
                    publish(
                        PushInnerNotificationEvent(
                            Notification(
                                message = message,
                                type = NotificationType.REGULAR,
                                lifespan = NotificationLifespan.MEDIUM
                            )
                        )
                    )
                }

                val response = lastfmClient.trackApi.scrobble(candidateScrobbles.map { it.toLastfmScrobble() })

                if (response.responseHolder.summary.ignored > 0) {
                    val message = "${response.responseHolder.summary.ignored} scrobbles were ignored by service"
                    logger.warn { message }
                    publish(
                        PushInnerNotificationEvent(
                            Notification(
                                message = message,
                                type = NotificationType.REGULAR,
                                lifespan = NotificationLifespan.INFINITE
                            )
                        )
                    )
                }

                lastfmScrobblerStorage.removeFromScrobbleCache(candidateScrobbles)
            }
        }
    }

    /**
     * Fulfill [pendingScrobble] with final details from [trackFinishEvent] and add it to [lastfmScrobblerStorage].
     */
    private fun completePendingScrobble(pendingScrobble: Scrobble, trackFinishEvent: TrackFinishedEvent) {
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
    private fun scrobbleDurationMeetsRequirements(scrobbleEvent: TrackFinishedEvent): Boolean {
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
            ).comply()
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
        } catch (e: LastfmSerializationException) {
            logger.error(e.reason) { "Failed to decode Last.fm API response: ${e.json}" }
            "Failed to call Last.fm service, please see logs for details"
        } catch (e: LastfmException) {
            logger.error(e) { "Exception caught while handling Last.fm API request" }
            "Failed to call Last.fm service, please see logs for details"
        } finally {
            logger.debug { "${lastfmScrobblerStorage.scrobbleCache().size} scrobbles in cache" }
        }.also {
            it?.let {
                publish(
                    PushInnerNotificationEvent(
                        Notification(
                            message = it,
                            type = NotificationType.ERROR,
                            lifespan = NotificationLifespan.LONG
                        )
                    )
                )
            }
        }
    }

    @Serializable
    data class LastfmScrobblerComponentConfiguration(
        var enabled: Boolean = false,
        var session: Session? = null,
        var autoScrobble: Boolean = true,
        var sendNowPlaying: Boolean = true
    ) : ComponentConfiguration

    override val componentConfiguration = mutableStateOf(
        readComponentConfiguration {
            LastfmScrobblerComponentConfiguration()
        }
    )

    val lastfmClient: LastfmClient = LastfmClient(
        apiKey = "3c3f2425f258b1bc2f7eddcd95194ef4",
        apiSecret = "a05c02f0b955060fc782f7a9270eeab6",
        session = componentConfiguration.value.session
    )

    inner class LastfmScrobblerSettingsState {

        fun updateSession(session: Session?) {
            componentConfiguration.value = componentConfiguration.value.copy(session = session)
            lastfmClient.session(session)
        }

        fun toggleAutoScrobble() {
            componentConfiguration.value = componentConfiguration.value.copy(autoScrobble = componentConfiguration.value.autoScrobble.not())
        }

        fun toggleSendNowPlaying() {
            componentConfiguration.value = componentConfiguration.value.copy(sendNowPlaying = componentConfiguration.value.sendNowPlaying.not())
        }

    }

    @Composable
    fun rememberLastfmScrobblerSettingsState(
    ) = remember {
        LastfmScrobblerSettingsState()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun composeSettings() {
        val state = rememberLastfmScrobblerSettingsState()
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
                    TextTooltip(
                        tooltip = "Session key used by Gravifon to access your Last.fm account"
                    ) {
                        Text("Session Key:")
                    }
                    BasicTextField(
                        value = componentConfiguration.value.session?.key ?: "",
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
                if (componentConfiguration.value.session == null) {
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
                        TextTooltip(
                            tooltip = "Please this URL to authorize Gravifon to access your Last.fm account"
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
                        IconButton(
                            enabled = authorization != null,
                            onClick = {
                                DesktopUtil.openInBrowser(authorization?.second.toString())
                            },
                            modifier = Modifier
                                .size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.OpenInBrowser,
                                contentDescription = "Open in Browser"
                            )
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
                        enabled = componentConfiguration.value.session == null && authorization == null,
                        onClick = {
                            error = handleLastfmException {
                                authorization = lastfmClient.authorizeStep1()
                            }
                        }
                    ) {
                        Text("Request Token")
                    }
                    if (componentConfiguration.value.session == null) {
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
                                checked = componentConfiguration.value.autoScrobble,
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
                                        if (componentConfiguration.value.autoScrobble) {
                                            state.toggleSendNowPlaying()
                                        }
                                    }
                                )
                        ) {
                            Checkbox(
                                enabled = componentConfiguration.value.autoScrobble,
                                checked = componentConfiguration.value.sendNowPlaying,
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