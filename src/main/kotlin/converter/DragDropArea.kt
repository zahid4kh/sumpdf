package converter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DragDropArea(
    modifier: Modifier = Modifier,
    onFilesDropped: (List<File>) -> Unit,
    content: @Composable () -> Unit
) {
    var isDragging by remember { mutableStateOf(false) }

    val borderModifier = if (isDragging) {
        modifier
            .padding(16.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)

    } else {
        modifier
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = MaterialTheme.shapes.medium
            )
    }

    Box(
        modifier = borderModifier
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = remember {
                    object : DragAndDropTarget {
                        override fun onStarted(event: DragAndDropEvent) {
                            isDragging = true
                        }

                        override fun onEnded(event: DragAndDropEvent) {
                            isDragging = false
                        }

                        override fun onDrop(event: DragAndDropEvent): Boolean {
                            val files = try {
                                if (event.awtTransferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                                    @Suppress("UNCHECKED_CAST")
                                    event.awtTransferable.getTransferData(
                                        DataFlavor.javaFileListFlavor
                                    ) as List<File>
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
            )
    ) {
        content()
    }
}