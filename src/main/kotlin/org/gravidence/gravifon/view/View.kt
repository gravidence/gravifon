package org.gravidence.gravifon.view

import androidx.compose.runtime.Composable
import org.gravidence.gravifon.event.EventHandler

abstract class View : EventHandler() {

    @Composable
    abstract fun compose()

}