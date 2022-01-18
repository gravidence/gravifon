package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable

@Serializable
data class VirtualTrack(
    val path: String,
    val fields: MutableMap<FieldKey, FieldValues>? = null
) {

    fun getArtist(): FieldValues? {
        return fields?.get(FieldKeyExt.ARTIST.name)
    }

}