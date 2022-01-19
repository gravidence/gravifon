package org.gravidence.gravifon.domain

import kotlinx.serialization.Serializable

@Serializable
sealed class VirtualTrack(
    open val fields: MutableMap<FieldKey, FieldValues>? = null
) : Track {

    fun getArtist(): FieldValues? {
        return fields?.get(FieldKeyExt.ARTIST.name)
    }

}