import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.io.File
import kotlin.math.roundToInt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun PdfListArea(
    pdfFiles: List<PdfFile>,
    onRemovePdf: (PdfFile) -> Unit,
    onFilesDropped: (List<File>) -> Unit = {},
    onReorderPdfs: (fromIndex: Int, toIndex: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }
    var draggedItemIndex by remember { mutableStateOf<Int?>(null) }

    val cardModifier = if (isDragging && draggedItemIndex == null) {
        modifier
            .fillMaxWidth()
            .height(160.dp)
            .border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(10.dp)
            )
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
    } else {
        modifier
            .fillMaxWidth()
            .height(160.dp)
    }

    Card(
        modifier = cardModifier
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            .dragAndDropTarget(
                shouldStartDragAndDrop = { event ->
                    draggedItemIndex == null && event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                },
                target = remember {
                    object : DragAndDropTarget {
                        override fun onStarted(event: DragAndDropEvent) {
                            if (draggedItemIndex == null) {
                                isDragging = true
                            }
                        }

                        override fun onEnded(event: DragAndDropEvent) {
                            if (draggedItemIndex == null) {
                                isDragging = false
                            }
                        }

                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            if (draggedItemIndex != null) return false

                            val files = try {
                                if (event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                                    @Suppress("UNCHECKED_CAST")
                                    val allFiles = event.awtTransferable.getTransferData(
                                        DataFlavor.javaFileListFlavor
                                    ) as List<File>

                                    allFiles.filter { it.extension.lowercase() == "pdf" }
                                } else {
                                    emptyList()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                emptyList()
                            }

                            if (files.isNotEmpty()) {
                                onFilesDropped(files)
                                return true
                            }
                            return false
                        }
                    }
                }
            ),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (isDragging && draggedItemIndex == null)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging && draggedItemIndex == null)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        if (pdfFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isDragging)
                        "Drop PDF files here!"
                    else
                        "Drag and drop PDF files here\nor click 'Add PDFs' button",
                    color = if (isDragging)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                val verticalScrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(verticalScrollState)
                ) {
                    DraggableGrid(
                        pdfFiles = pdfFiles,
                        draggedItemIndex = draggedItemIndex,
                        onDragStart = { index -> draggedItemIndex = index },
                        onDragEnd = { draggedItemIndex = null },
                        onReorder = onReorderPdfs,
                        onRemovePdf = onRemovePdf,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 32.dp, top = 20.dp, bottom = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .fillMaxHeight()
                        .padding(end = 4.dp),
                    adapter = rememberScrollbarAdapter(verticalScrollState),
                    style = LocalScrollbarStyle.current.copy(
                        hoverColor = MaterialTheme.colorScheme.outline,
                        unhoverColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        if (isDragging && draggedItemIndex == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (pdfFiles.isNotEmpty()) {
                    Text(
                        text = "Drop PDF files to add them",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 18.sp,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun DraggableGrid(
    pdfFiles: List<PdfFile>,
    draggedItemIndex: Int?,
    onDragStart: (Int) -> Unit,
    onDragEnd: () -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onRemovePdf: (PdfFile) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        val itemWidth = 200.dp
        val itemHeight = 120.dp
        val spacing = 15.dp

        val itemsPerRow = ((maxWidth + spacing) / (itemWidth + spacing)).toInt().coerceAtLeast(1)

        val totalRows = (pdfFiles.size + itemsPerRow - 1) / itemsPerRow
        val totalHeight = totalRows * (itemHeight + spacing) - spacing

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
        ) {
            pdfFiles.forEachIndexed { index, pdfFile ->
                val row = index / itemsPerRow
                val col = index % itemsPerRow

                val isBeingDragged = draggedItemIndex == index

                DraggablePdfItem(
                    pdfFile = pdfFile,
                    index = index,
                    gridPosition = IntOffset(
                        x = (col * (itemWidth + spacing).value).toInt(),
                        y = (row * (itemHeight + spacing).value).toInt()
                    ),
                    isBeingDragged = isBeingDragged,
                    itemWidth = itemWidth,
                    itemHeight = itemHeight,
                    onRemove = { onRemovePdf(pdfFile) },
                    onDragStart = { onDragStart(index) },
                    onDragEnd = { fromIndex, dragOffset ->
                        val newCol = ((col * (itemWidth + spacing).value + dragOffset.x) / (itemWidth + spacing).value)
                            .roundToInt().coerceIn(0, itemsPerRow - 1)
                        val newRow = ((row * (itemHeight + spacing).value + dragOffset.y) / (itemHeight + spacing).value)
                            .roundToInt().coerceAtLeast(0)

                        val newIndex = (newRow * itemsPerRow + newCol).coerceIn(0, pdfFiles.lastIndex)

                        if (fromIndex != newIndex) {
                            onReorder(fromIndex, newIndex)
                        }
                        onDragEnd()
                    }
                )
            }
        }
    }
}

@Composable
private fun DraggablePdfItem(
    pdfFile: PdfFile,
    index: Int,
    gridPosition: IntOffset,
    isBeingDragged: Boolean,
    itemWidth: androidx.compose.ui.unit.Dp,
    itemHeight: androidx.compose.ui.unit.Dp,
    onRemove: () -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: (fromIndex: Int, dragOffset: Offset) -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = gridPosition.x + if (isBeingDragged) offsetX.roundToInt() else 0,
                    y = gridPosition.y + if (isBeingDragged) offsetY.roundToInt() else 0
                )
            }
            .size(itemWidth, itemHeight)
            .graphicsLayer {
                alpha = if (isBeingDragged) 0.8f else 1f
                scaleX = if (isBeingDragged) 1.05f else 1f
                scaleY = if (isBeingDragged) 1.05f else 1f
            }
            .zIndex(if (isBeingDragged) 1f else 0f)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        offsetX = 0f
                        offsetY = 0f
                        onDragStart()
                    },
                    onDragEnd = {
                        onDragEnd(index, Offset(offsetX, offsetY))
                        offsetX = 0f
                        offsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                )
            }
    ) {
        PdfItemCard(
            pdfFile = pdfFile,
            onRemove = onRemove,
            modifier = Modifier.pointerHoverIcon(
                PointerIcon(Cursor(Cursor.MOVE_CURSOR))
            )
        )
    }
}