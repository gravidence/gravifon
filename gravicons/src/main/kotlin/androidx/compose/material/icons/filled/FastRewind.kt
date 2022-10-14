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

public val Icons.Filled.FastRewind: ImageVector
    get() {
        if (_fastRewind != null) {
            return _fastRewind!!
        }
        _fastRewind = materialIcon(name = "Filled.FastRewind") {
            materialPath {
                moveTo(11.0f, 18.0f)
                lineTo(11.0f, 6.0f)
                lineToRelative(-8.5f, 6.0f)
                lineToRelative(8.5f, 6.0f)
                close()
                moveTo(11.5f, 12.0f)
                lineToRelative(8.5f, 6.0f)
                lineTo(20.0f, 6.0f)
                lineToRelative(-8.5f, 6.0f)
                close()
            }
        }
        return _fastRewind!!
    }

private var _fastRewind: ImageVector? = null
