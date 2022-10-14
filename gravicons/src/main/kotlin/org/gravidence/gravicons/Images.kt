package org.gravidence.gravicons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import org.springframework.core.io.ClassPathResource
import java.nio.file.Path
import kotlin.io.path.pathString

fun loadImageBitmapFromClassPath(path: Path): ImageBitmap {
    return loadImageBitmap(ClassPathResource(path.pathString).inputStream)
}