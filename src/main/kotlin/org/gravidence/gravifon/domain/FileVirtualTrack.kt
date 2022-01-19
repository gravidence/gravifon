package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.io.File
import java.net.URI

@Serializable
class FileVirtualTrack(
    val path: String,
    @Transient override val fields: MutableMap<FieldKey, FieldValues>? = null
) : VirtualTrack(fields = fields) {

    override fun uri(): URI {
        return File(path).toURI()
    }

}