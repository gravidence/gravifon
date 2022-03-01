package org.gravidence.gravifon.util

import mu.KotlinLogging
import java.awt.Desktop
import java.net.URI

private val logger = KotlinLogging.logger {}

object DesktopUtil {

    fun openInBrowser(url: String) {
        try {
            Desktop.getDesktop().browse(URI(url))
        } catch (e: Exception) {
            logger.warn(e) { "Failed to open URL in browser: $url" }
        }
    }

}