import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
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
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor
import java.awt.datatransfer.DataFlavor
import java.io.File

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun PdfListArea(
    pdfFiles: List<PdfFile>,
    onRemovePdf: (PdfFile) -> Unit,
    onFilesDropped: (List<File>) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDragging by remember { mutableStateOf(false) }

    val cardModifier = if (isDragging) {
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
            color = if (isDragging)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging)
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
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateContentSize()
                            .padding(start = 20.dp, end = 32.dp, top = 20.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(15.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp),
                    ){
                        pdfFiles.forEach {
                            PdfItemCard(
                                pdfFile = it,
                                onRemove = { onRemovePdf(it) }
                            )
                        }
                    }

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

        if (isDragging) {
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