package org.gravidence.gravifon.ui.image

import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import java.nio.file.Path

@Composable
fun AppIcon(path: String, modifier: Modifier = Modifier, tint: Color? = null) {
    loadImageBitmapFromClassPath(Path.of("icons", path))?.let {
        // TODO workaround with if clause, so that theme defaults are not overridden; there must be a solution
        if (tint == null) {
            Icon(
                bitmap = it,
                contentDescription = path,
                modifier = modifier
            )
        } else {
            Icon(
                bitmap = it,
                contentDescription = path,
                modifier = modifier,
                tint = tint
            )
        }
    }
}