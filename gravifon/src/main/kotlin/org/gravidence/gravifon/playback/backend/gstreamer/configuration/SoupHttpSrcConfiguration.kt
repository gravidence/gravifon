package org.gravidence.gravifon.playback.backend.gstreamer.configuration

import mu.KotlinLogging
import org.freedesktop.gstreamer.Element
import org.gravidence.gravifon.playback.backend.gstreamer.GstreamerAudioBackend
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

/**
 * Prepares [souphttpsrc properties](https://gstreamer.freedesktop.org/documentation/soup/souphttpsrc.html?gi-language=c#properties).
 */
@Component
class SoupHttpSrcConfiguration(override val typeName: String = "GstSoupHTTPSrc") : ElementConfiguration {

    override fun apply(element: Element, backendConfiguration: GstreamerAudioBackend.GstreamerComponentConfiguration) {
        try {
            element.set("ssl-strict", backendConfiguration.sslStrict)
        } catch (e: IllegalArgumentException) {
            logger.error(e) { "Failed to set 'ssl-strict' property" }
        }
    }

}