package org.gravidence.gravifon.plugin.bandcamp.model

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

val bandcampSerializer = Json { ignoreUnknownKeys = true }

object InstantAsRFC1123StringSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        val date = ZonedDateTime.parse(decoder.decodeString(), DateTimeFormatter.RFC_1123_DATE_TIME)
        return Instant.fromEpochSeconds(date.toEpochSecond())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(DateTimeFormatter.RFC_1123_DATE_TIME.format(value.toJavaInstant()))
    }

}