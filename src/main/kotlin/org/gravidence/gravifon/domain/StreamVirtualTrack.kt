package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
class StreamVirtualTrack(
    val url: String,
    override val fields: MutableMap<FieldKey, FieldValues>? = null
) : VirtualTrack() {

    override fun uri(): URI {
        return URI(url)
    }

}