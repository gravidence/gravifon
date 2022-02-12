package org.gravidence.gravifon.plugin.scrobble

import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.track.VirtualTrack
import kotlin.time.Duration

@Serializable
data class Scrobble(
    val track: VirtualTrack,
    @Contextual
    var duration: Duration? = null,
    val startedAt: Instant,
    var finishedAt: Instant? = null,
)