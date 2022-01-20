package org.gravidence.gravifon.domain.track

import java.net.URI

sealed interface Track {

    fun uri(): URI

}