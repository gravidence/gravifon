package org.gravidence.gravifon.playback.backend.gstreamer.configuration

import org.freedesktop.gstreamer.Element
import org.gravidence.gravifon.playback.backend.gstreamer.GstreamerAudioBackend

/**
 * Gstreamer Element fine tuning handler.
 */
interface ElementConfiguration {

    val typeName: String

    fun apply(element: Element, backendConfiguration: GstreamerAudioBackend.GstreamerComponentConfiguration)

}