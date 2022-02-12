package org.gravidence.gravifon.domain.header

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Serializable
data class Headers(
    @Contextual
    val length: Duration? = null
)
