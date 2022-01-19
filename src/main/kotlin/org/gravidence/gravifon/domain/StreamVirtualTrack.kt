package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.net.URI

@Serializable
class StreamVirtualTrack(
    val url: String,
    @Transient override val fields: MutableMap<FieldKey, FieldValues>? = null
) : VirtualTrack(fields = fields) {

    override fun uri(): URI {
        return URI(url)
    }

}