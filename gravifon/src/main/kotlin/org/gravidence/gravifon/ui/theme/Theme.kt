@file:OptIn(ExperimentalUnitApi::class)

package org.gravidence.gravifon.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.*

val gListItemColor: Color = Color.LightGray.copy(alpha = 0.3f)
val gSelectedListItemColor: Color = Color.LightGray.copy(alpha = 0.7f)
val gListHeaderColor: Color = Color.LightGray

val gShape: Shape = RoundedCornerShape(5.dp)

val gTextFieldColor: Color = Color.LightGray.copy(alpha = 0.3f)
val gTextFieldStyle: TextStyle = TextStyle(fontSize = 16.sp)

val bigFont: TextUnit = TextUnit(1.2f, TextUnitType.Em)
val smallFont: TextUnit = TextUnit(0.9f, TextUnitType.Em)