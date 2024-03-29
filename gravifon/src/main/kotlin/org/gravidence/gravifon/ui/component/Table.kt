@file:OptIn(ExperimentalComposeUiApi::class)

package org.gravidence.gravifon.ui.component

import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.gravidence.gravifon.playlist.layout.ScrollPosition
import org.gravidence.gravifon.ui.theme.gListHeaderColor
import org.gravidence.gravifon.ui.theme.gListItemColor
import org.gravidence.gravifon.ui.theme.gSelectedListItemColor
import org.gravidence.gravifon.ui.theme.gShape
import java.awt.Cursor
import java.awt.event.MouseEvent
import kotlin.math.max
import kotlin.math.min

open class TableState<T>(
    val layout: MutableState<TableLayout> = mutableStateOf(TableLayout()),
    val enabled: MutableState<Boolean> = mutableStateOf(false),
    val readOnly: MutableState<Boolean> = mutableStateOf(false),
    val multiSelection: MutableState<Boolean> = mutableStateOf(true),
    val grid: MutableState<TableGrid<T>?> = mutableStateOf(null),
    val selectedRows: MutableState<Set<Int>> = mutableStateOf(setOf()),
    val initialVerticalScrollPosition: ScrollPosition = ScrollPosition(),
//    val initialHorizontalScrollPosition: ScrollPosition = ScrollPosition(),
) {

    /**
     * @return true when event is handled
     */
    open fun onKeyEvent(keyEvent: KeyEvent): Boolean {
        return if (keyEvent.type == KeyEventType.KeyUp) {
            when (keyEvent.key) {
                Key.A -> {
                    if (keyEvent.isCtrlPressed) {
                        grid.value?.rows?.value?.let { rows ->
                            selectedRows.value = List(rows.size) { i -> i }.toSet()
                        }
                        true
                    } else {
                        false
                    }
                }
                else -> false
            }
        } else {
            false
        }
    }

    open fun onRowRelease(rowIndex: Int, pointerEvent: PointerEvent) {
        (pointerEvent.nativeEvent as? MouseEvent)?.let {
            if (multiSelection.value) {
                if (it.button == 1 && it.clickCount == 1 && it.isControlDown) {
                    if (selectedRows.value.contains(rowIndex)) {
                        selectedRows.value -= rowIndex
                    } else {
                        selectedRows.value += rowIndex
                    }
                } else if (it.button == 1 && it.clickCount == 1 && it.isShiftDown && selectedRows.value.isNotEmpty()) {
                    val indexOfFirstSelected = selectedRows.value.minOf { indexOfSelected -> indexOfSelected }
                    for (i in min(rowIndex, indexOfFirstSelected)..max(rowIndex, indexOfFirstSelected)) {
                        selectedRows.value += i
                    }
                } else if (it.button == 1 && it.clickCount == 1 && !it.isControlDown) {
                    selectedRows.value = setOf(rowIndex)
                }
            } else {
                if (it.button == 1 && it.clickCount == 1) {
                    selectedRows.value = setOf(rowIndex)
                }
            }
        }
    }

    open fun onRowPress(rowIndex: Int, pointerEvent: PointerEvent) {
        if (pointerEvent.buttons.isSecondaryPressed) {
            if (!selectedRows.value.contains(rowIndex)) {
                selectedRows.value = setOf(rowIndex)
            }
        }
    }

    open fun onCellChange(cell: TableCell<T>, rowIndex: Int, columnIndex: Int, newValue: String) {
        cellState(this, rowIndex, columnIndex)?.let {
            it.value = it.value.copy(value = newValue)
        }
    }

    open fun onVerticalScroll(scrollPosition: ScrollPosition) {
        // do nothing by default
    }

    open fun onTableColumnWidthChange(index: Int, delta: Dp) {
        val columnsUpdated = layout.value.columns.toMutableList().apply {
            set(index, this[index].copy(width = this[index].width!! + delta))
        }
        layout.value = layout.value.copy(columns = columnsUpdated)
    }

    companion object {

        fun <T> cellState(tableState: TableState<T>, rowIndex: Int, columnIndex: Int): MutableState<TableCell<T>>? {
            return tableState.grid.value?.let {
                it.rows.value[rowIndex].cells[columnIndex]
            }
        }

    }

}

@Composable
fun <T> Table(tableState: TableState<T>) {
    val baseModifier = if (!tableState.enabled.value) {
        // make static (non-enabled) table focusable, in order to handle keyboard events
        val focusRequester = remember { FocusRequester() }
        Modifier
            .focusable()
            .focusRequester(focusRequester)
            .onPreviewKeyEvent {
                tableState.onKeyEvent(it)
            }
            .onPointerEvent(
                eventType = PointerEventType.Press
            ) {
                focusRequester.requestFocus()
            }
            .onPointerEvent(
                eventType = PointerEventType.Scroll
            ) {
                focusRequester.requestFocus()
            }
    } else {
        Modifier
    }

    Box(
        modifier = baseModifier
            .padding(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.focusable()
        ) {
            TableHeader(tableState)
            TableContent(tableState)
        }
    }
}

@Composable
fun <T> TableHeader(tableState: TableState<T>) {
    if (tableState.layout.value.displayHeaders) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            tableState.layout.value.columns.forEachIndexed { index, column ->
                val columnModifier = if (column.fraction != null) {
                    Modifier.fillMaxWidth(column.fraction)
                } else if (column.width != null) {
                    Modifier.width(column.width)
                } else {
                    Modifier
                }
                Text(
                    text = column.header ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    fontWeight = FontWeight.Bold,
                    modifier = columnModifier
                        .background(color = gListHeaderColor, shape = gShape)
                        .padding(5.dp)
                )

                val spacerModifier = if (column.width != null) {
                    Modifier
                        .pointerHoverIcon(PointerIcon(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR)))
                        .draggable(
                            state = rememberDraggableState { tableState.onTableColumnWidthChange(index, it.dp) },
                            orientation = Orientation.Horizontal,
                            startDragImmediately = true
                        )
                } else {
                    Modifier
                }
                Spacer(
                    modifier = spacerModifier
                        .width(5.dp)
                        .height(10.dp)
                )
            }
        }
    }
}

@Composable
fun <T> TableContent(tableState: TableState<T>) {
    val verticalScrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = tableState.initialVerticalScrollPosition.index,
        initialFirstVisibleItemScrollOffset = tableState.initialVerticalScrollPosition.scrollOffset
    )

    remember(verticalScrollState.firstVisibleItemIndex, verticalScrollState.firstVisibleItemScrollOffset) {
        tableState.onVerticalScroll(
            ScrollPosition(index = verticalScrollState.firstVisibleItemIndex, scrollOffset = verticalScrollState.firstVisibleItemScrollOffset)
        )
    }

    Box {
        LazyColumn(
            state = verticalScrollState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            tableContent(tableState)
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(verticalScrollState),
            modifier = Modifier
                .align(Alignment.TopEnd)
        )
    }
}

fun <T> LazyListScope.tableContent(tableState: TableState<T>) {
    tableState.grid.value?.rows?.value?.let { rows ->
        itemsIndexed(
            items = rows,
            // without the key, lazy list does nothing else but re-uses items by index (ignoring actual backing list content changes)
            key = { rowIndex, row -> rowIndex.toString() + row.toString() }
        ) { rowIndex, row ->
            if (rowIndex > 0) {
                Spacer(Modifier.height(5.dp).fillMaxWidth())
            }

            val rowModifier = if (rowIndex in tableState.selectedRows.value) {
                Modifier
                    .background(color = gSelectedListItemColor, shape = gShape)
            } else {
                Modifier
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = rowModifier
                    .onPointerEvent(
                        eventType = PointerEventType.Release,
                        onEvent = { tableState.onRowRelease(rowIndex, it) }
                    )
                    .onPointerEvent(
                        eventType = PointerEventType.Press,
                        onEvent = { tableState.onRowPress(rowIndex, it) }
                    )
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
                        with(row.cells[columnIndex]) {
                            value.content(this, rowIndex, columnIndex, tableState)
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
    val value: String?,
    var enabled: Boolean? = null,
    var readOnly: Boolean? = null,
    val source: T? = null,
    // TODO pass mutable state of self to re-render cell on state change, but in fact whole shebang should be rewritten
    val content: @Composable ((cell: MutableState<TableCell<T>>, rowIndex: Int, columnIndex: Int, tableState: TableState<T>) -> Unit) = { cell, rowIndex, columnIndex, tableState ->
        if (enabled ?: tableState.enabled.value) {
            BasicTextField(
                value = cell.value.value ?: "<varies>",
                singleLine = true,
                readOnly = readOnly ?: tableState.readOnly.value,
                modifier = Modifier
                    .background(color = gListItemColor, shape = gShape)
                    .padding(5.dp)
                    .fillMaxWidth(),
                onValueChange = { newValue ->
                    tableState.onCellChange(cell.value, rowIndex, columnIndex, newValue)
                }
            )
        } else {
            Text(
                text = cell.value.value ?: "<varies>",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .background(color = gListItemColor, shape = gShape)
                    .padding(5.dp)
                    .fillMaxWidth(),
            )
        }
    },
)

class TableRow<T>(
    val cells: MutableList<MutableState<TableCell<T>>>
)

class TableGrid<T>(
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