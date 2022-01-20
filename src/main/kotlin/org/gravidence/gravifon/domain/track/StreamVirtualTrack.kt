package org.gravidence.gravifon.domain.track

import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.tag.FieldKey
import org.gravidence.gravifon.domain.tag.FieldValues
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