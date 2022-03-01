package org.gravidence.gravifon.domain.track

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.header.Headers
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.tag.FieldKey
import java.net.URI

@Serializable
@SerialName("stream")
data class StreamVirtualTrack(
    val sourceUrl: String,
    val streamUrl: String,
    override val headers: Headers = Headers(),
    override val fields: MutableMap<FieldKey, FieldValues> = mutableMapOf(),
    override val customFields: MutableMap<String, FieldValues> = mutableMapOf()
) : VirtualTrack() {

    override fun uri(): URI {
        return URI(streamUrl)
    }

}