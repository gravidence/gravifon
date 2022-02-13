package org.gravidence.lastfm4k.misc

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.body.form
import java.security.MessageDigest

fun signatureKey(params: List<Param?>, apiSecret: String): String {
    return params
        .filterNotNull()
        .sortedBy { it.key }
        .joinToString(separator = "") { it.key + it.value }
        .plus(apiSecret)
        .toMD5()
}

fun Request.lfmQueryParams(params: List<Param?>): Request {
    var targetRequest: Request = this

    params
        .filterNotNull()
        .forEach { targetRequest = targetRequest.query(it.key, it.value) }

    return targetRequest
}

fun Request.lfmQuerySignature(params: List<Param?>, apiSecret: String): Request {
    return this.query("api_sig", signatureKey(params, apiSecret))
}

fun Request.lfmFormParams(params: List<Param?>): Request {
    var targetRequest: Request = this

    params
        .filterNotNull()
        .forEach { targetRequest = targetRequest.form(it.key, it.value) }

    return targetRequest
}

fun Request.lfmFormSignature(params: List<Param?>, apiSecret: String): Request {
    return this.form("api_sig", signatureKey(params, apiSecret))
}

fun Response.toJsonObject(): JsonObject {
    return lastfmSerializer.parseToJsonElement(this.bodyString()).jsonObject
}

fun String.toMD5(): String {
    val bytes = MessageDigest.getInstance("MD5").digest(this.toByteArray())
    return bytes.toHex()
}

fun ByteArray.toHex(): String {
    return joinToString("") { "%02x".format(it) }
}

fun Instant.toLocalDateTime(): LocalDateTime {
    return toLocalDateTime(TimeZone.currentSystemDefault())
}