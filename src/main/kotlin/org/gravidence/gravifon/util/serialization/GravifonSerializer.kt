package org.gravidence.gravifon.util.serialization

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual

private val module = SerializersModule {
    contextual(DurationAsStringSerializer)
}

val gravifonSerializer = Json { serializersModule = module }