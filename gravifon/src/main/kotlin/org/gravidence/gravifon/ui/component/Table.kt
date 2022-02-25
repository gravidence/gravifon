@file:OptIn(ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.ui.theme.gListHeaderColor
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gShape

typealias TableCell = String
typealias TableGrid = MutableList<MutableList<MutableState<TableCell>>>

@Composable
fun Table(
    layout: TableLayout = TableLayout(),
    readOnly: Boolean = true,
    grid: TableGrid? = null,
    onRowClick: (rowIndex: Int) -> Unit = ::onRowClick,
    onCellChange: (rowIndex: Int, columnIndex: Int, newValue: String) -> Unit = ::onCellChange
) {
    Box(
        modifier = Modifier
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            layout.columns.forEachIndexed { columnIndex, column ->
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
                    if (layout.displayHeaders) {
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
                    grid?.forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier
                                .onPointerEvent(
                                    eventType = PointerEventType.Release,
                                    onEvent = { onRowClick(rowIndex) }
                                )
                        ) {
                            BasicTextField(
                                value = row[columnIndex].value,
                                singleLine = true,
                                readOnly = readOnly,
                                modifier = Modifier
                                    .background(color = gListItemColor, shape = gShape)
                                    .padding(5.dp)
                                    .fillMaxWidth(),
                                onValueChange = { onCellChange(rowIndex, columnIndex, it) }
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

private fun onRowClick(rowIndex: Int) {

}

private fun onCellChange(rowIndex: Int, columnIndex: Int, newValue: String) {

}