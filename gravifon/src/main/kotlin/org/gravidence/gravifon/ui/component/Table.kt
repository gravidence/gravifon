@file:OptIn(ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.ui.theme.gListHeaderColor
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gSelectedListItemColor
import org.gravidence.gravifon.ui.theme.gShape
import java.awt.event.MouseEvent

open class TableState<T>(
    val layout: MutableState<TableLayout> = mutableStateOf(TableLayout()),
    val enabled: MutableState<Boolean> = mutableStateOf(false),
    val readOnly: MutableState<Boolean> = mutableStateOf(true),
    val grid: MutableState<TableGrid<T>?> = mutableStateOf(null),
    val selectedRows: MutableState<Set<Int>> = mutableStateOf(setOf())
) {

    open fun onRowClick(rowIndex: Int, pointerEvent: PointerEvent) {
        (pointerEvent.nativeEvent as? MouseEvent)?.let {
            if (it.button == 1 && it.clickCount == 1 && !it.isControlDown) {
                selectedRows.value = setOf(rowIndex)
            } else if (it.button == 1 && it.clickCount == 1 && it.isControlDown) {
                if (selectedRows.value.contains(rowIndex)) {
                    selectedRows.value -= rowIndex
                } else {
                    selectedRows.value += rowIndex
                }
            }
        }
    }

    open fun onCellChange(cell: TableCell<T>, rowIndex: Int, columnIndex: Int, newValue: String) {
        grid.value?.let {
            it.rows.value[rowIndex].cells[columnIndex].value = cell.copy(content = newValue)
        }
    }

}

@Composable
fun <T> Table(tableState: TableState<T>) {
    Box(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            tableState.layout.value.columns.forEachIndexed { columnIndex, column ->
                val columnModifier = if (column.fraction != null) {
                    Modifier.fillMaxWidth(column.fraction)
                } else if (column.width != null) {
                    Modifier.width(column.width)
                } else {
                    Modifier
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = columnModifier
                ) {
                    if (tableState.layout.value.displayHeaders) {
                        Row {
                            Text(
                                text = column.header ?: "",
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(color = gListHeaderColor, shape = gShape)
                                    .padding(5.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                    tableState.grid.value?.rows?.value?.forEachIndexed { rowIndex, row ->
                        val rowModifier = if (rowIndex in tableState.selectedRows.value) {
                            Modifier.background(color = gSelectedListItemColor, shape = gShape)
                        } else {
                            Modifier
                        }

                        Row(
                            modifier = rowModifier
                                .onPointerEvent(
                                    eventType = PointerEventType.Release,
                                    onEvent = { tableState.onRowClick(rowIndex, it) }
                                )
                        ) {
                            val cell = row.cells[columnIndex].value
                            BasicTextField(
                                value = cell.content ?: "<varies>",
                                singleLine = true,
                                enabled = tableState.enabled.value,
                                readOnly = tableState.readOnly.value,
                                modifier = Modifier
                                    .background(color = gListItemColor, shape = gShape)
                                    .padding(5.dp)
                                    .fillMaxWidth(),
                                onValueChange = { tableState.onCellChange(cell, rowIndex, columnIndex, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TableLayout(
    val displayHeaders: Boolean = false,
    val columns: List<TableColumn> = listOf(),
)

data class TableColumn(
    val header: String? = null,
    val width: Dp? = null,
    val fraction: Float? = null
)

data class TableCell<T>(
    var content: String?,
    val source: T? = null
)

data class TableRow<T>(
    val cells: MutableList<MutableState<TableCell<T>>>
)

data class TableGrid<T>(
    val rows: MutableState<MutableList<TableRow<T>>>
)

fun <T> singleColumnTableGrid(rows: List<TableCell<T>>): TableGrid<T> {
    return TableGrid(
        rows = mutableStateOf(
            rows.map {
                TableRow(cells = mutableListOf(mutableStateOf(it)))
            }.toMutableList()
        )
    )
}