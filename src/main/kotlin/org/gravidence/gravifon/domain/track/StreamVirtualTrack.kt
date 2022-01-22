package org.gravidence.gravifon.domain.track

import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.tag.FieldKey
import java.net.URI

@Serializable
data class StreamVirtualTrack(
    val url: String,
    override val fields: MutableMap<FieldKey, FieldValues> = mutableMapOf()
) : VirtualTrack() {

    override fun uri(): URI {
        return URI(url)
    }

}