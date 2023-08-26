package org.gravidence.gravifon.util

import com.sun.jna.Platform
import mu.KotlinLogging
import java.awt.Desktop
import java.net.URI

private val logger = KotlinLogging.logger {}

object DesktopUtil {

    fun openInBrowser(url: String) {
        try {
            // use system specific 'open' command on Linux as workaround if Gnome libraries aren't present
            // see https://stackoverflow.com/questions/18004150/desktop-api-is-not-supported-on-the-current-platform
            // and https://wiki.archlinux.org/title/Xdg-utils#xdg-open
            if (Platform.isLinux()) {
                Runtime.getRuntime().exec(arrayOf("xdg-open", url))
            } else {
                Desktop.getDesktop().browse(URI(url))
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to open URL in browser: $url" }
        }
    }

}