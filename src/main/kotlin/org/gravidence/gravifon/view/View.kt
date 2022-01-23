package org.gravidence.gravifon.view

import org.gravidence.gravifon.event.EventHandler

abstract class View : EventHandler() {

    abstract var playlistId: String?

//    @Composable
//    abstract fun compose()

}