package org.gravidence.gravifon.domain

import java.net.URI

sealed interface Track {

    fun uri(): URI

}