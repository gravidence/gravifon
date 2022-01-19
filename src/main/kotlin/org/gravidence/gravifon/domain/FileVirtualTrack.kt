package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable
import java.io.File
import java.net.URI

@Serializable
class FileVirtualTrack(
    val path: String,
    override val fields: MutableMap<FieldKey, FieldValues>? = null
) : VirtualTrack() {

    override fun uri(): URI {
        return File(path).toURI()
    }

}