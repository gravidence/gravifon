package org.gravidence.gravifon.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isPrimaryPressed
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

class DropdownFieldState(
    val label: String,
    val readOnly: Boolean = true,
    val expanded: MutableState<Boolean> = mutableStateOf(false),
    val items: List<String>,
    val selectedItemIndex: MutableState<Int> = mutableStateOf(0),
) {

    val fieldSize = mutableStateOf(Size.Zero)

}

@Composable
fun DropdownField(dropdownFieldState: DropdownFieldState) {
    Column {
        OutlinedTextField(
            value = dropdownFieldState.items[dropdownFieldState.selectedItemIndex.value],
            readOnly = dropdownFieldState.readOnly,
            onValueChange = {},
            modifier = fieldBaseModifier(dropdownFieldState)
                .fillMaxWidth()
                .onGloballyPositioned { dropdownFieldState.fieldSize.value = it.size.toSize() },
            label = {
                Text(dropdownFieldState.label)
            },
            trailingIcon = {
                Icon(
                    imageVector = trailingIcon(dropdownFieldState),
                    contentDescription = "Dropdown menu",
                    modifier = trailingIconBaseModifier(dropdownFieldState)
                )
            }
        )
        DropdownMenu(
            expanded = dropdownFieldState.expanded.value,
            onDismissRequest = { dropdownFieldState.expanded.value = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { dropdownFieldState.fieldSize.value.width.toDp() })
        ) {
            dropdownFieldState.items.forEachIndexed { index, label ->
                DropdownMenuItem(
                    onClick = {
                        dropdownFieldState.selectedItemIndex.value = index
                        dropdownFieldState.expanded.value = false
                    }
                ) {
                    Row {
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                        if (dropdownFieldState.selectedItemIndex.value == index) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Selected item"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun fieldBaseModifier(dropdownFieldState: DropdownFieldState): Modifier {
    return if (dropdownFieldState.readOnly) {
        Modifier.onPointerEvent(eventType = PointerEventType.Press) {
            if (it.buttons.isPrimaryPressed) {
                dropdownFieldState.expanded.value = !dropdownFieldState.expanded.value
            }
        }
    } else {
        Modifier
    }
}

private fun trailingIconBaseModifier(dropdownFieldState: DropdownFieldState): Modifier {
    return if (!dropdownFieldState.readOnly) {
        Modifier.clickable {
            dropdownFieldState.expanded.value = !dropdownFieldState.expanded.value
        }
    } else {
        Modifier
    }
}

private fun trailingIcon(dropdownFieldState: DropdownFieldState): ImageVector {
    return if (dropdownFieldState.expanded.value)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown
}