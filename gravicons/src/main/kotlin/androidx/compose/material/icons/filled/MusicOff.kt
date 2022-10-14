/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.compose.material.icons.filled

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.MusicOff: ImageVector
    get() {
        if (_musicOff != null) {
            return _musicOff!!
        }
        _musicOff = materialIcon(name = "Filled.MusicOff") {
            materialPath {
                moveTo(4.27f, 3.0f)
                lineTo(3.0f, 4.27f)
                lineToRelative(9.0f, 9.0f)
                verticalLineToRelative(0.28f)
                curveToRelative(-0.59f, -0.34f, -1.27f, -0.55f, -2.0f, -0.55f)
                curveToRelative(-2.21f, 0.0f, -4.0f, 1.79f, -4.0f, 4.0f)
                reflectiveCurveToRelative(1.79f, 4.0f, 4.0f, 4.0f)
                reflectiveCurveToRelative(4.0f, -1.79f, 4.0f, -4.0f)
                verticalLineToRelative(-1.73f)
                lineTo(19.73f, 21.0f)
                lineTo(21.0f, 19.73f)
                lineTo(4.27f, 3.0f)
                close()
                moveTo(14.0f, 7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-6.0f)
                verticalLineToRelative(5.18f)
                lineToRelative(2.0f, 2.0f)
                close()
            }
        }
        return _musicOff!!
    }

private var _musicOff: ImageVector? = null
