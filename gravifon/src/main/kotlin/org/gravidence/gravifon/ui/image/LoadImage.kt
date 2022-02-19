package org.gravidence.gravifon.ui.image

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import mu.KotlinLogging
import org.springframework.core.io.ClassPathResource
import java.nio.file.Path
import kotlin.io.path.pathString

private val logger = KotlinLogging.logger {}

fun loadImageBitmapFromClassPath(path: Path): ImageBitmap? {
    return try {
        loadImageBitmap(ClassPathResource(path.pathString).inputStream)
    } catch (e: Exception) {
        logger.error(e) { "Failed to load image: $path" }
        null
    }
}