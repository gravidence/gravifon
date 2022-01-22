package org.gravidence.gravifon.domain.track

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.gravidence.gravifon.domain.tag.FieldValues
import org.jaudiotagger.tag.FieldKey
import java.io.File
import java.net.URI

@Serializable
@SerialName("file")
data class FileVirtualTrack(
    val path: String,
    override val fields: MutableMap<FieldKey, FieldValues> = mutableMapOf()
) : VirtualTrack() {

    override fun uri(): URI {
        return File(path).toURI()
    }

}