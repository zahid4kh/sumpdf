package splitter

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.dp
import converter.DragDropArea
import deskit.dialogs.InfoDialog
import deskit.dialogs.file.FileChooserDialog
import deskit.dialogs.file.FolderChooserDialog
import kotlinx.coroutines.delay
import splitter.formatFileSize

@Composable
fun SplitterContent(
    viewModel: SplitterViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    uiState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            delay(5000)
            viewModel.handleIntent(SplitterIntent.ClearMessages)
        }
    }

    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            delay(5000)
            viewModel.handleIntent(SplitterIntent.ClearMessages)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(30.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Select a PDF file to split into individual pages",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButtonOption(
                selected = uiState.splitMode == SplitMode.SAVE_ALL,
                onClick = { viewModel.handleIntent(SplitterIntent.SelectSplitMode(SplitMode.SAVE_ALL)) },
                text = "Save all splitted pages"
            )

            RadioButtonOption(
                selected = uiState.splitMode == SplitMode.DELETE_PAGES,
                onClick = { viewModel.handleIntent(SplitterIntent.SelectSplitMode(SplitMode.DELETE_PAGES)) },
                text = "Delete some pages after splitting"
            )

            AnimatedVisibility(
                visible = uiState.splitMode == SplitMode.DELETE_PAGES || uiState.splitMode == SplitMode.MERGE_PAGES,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut()
            ) {
                RadioButtonOption(
                    selected = uiState.splitMode == SplitMode.MERGE_PAGES,
                    onClick = { viewModel.handleIntent(SplitterIntent.SelectSplitMode(SplitMode.MERGE_PAGES)) },
                    text = "Merge pages"
                )
            }
        }

        if (uiState.extractedPages.isEmpty()) {
            DragDropArea(
                modifier = Modifier.weight(1f),
                onFilesDropped = { files ->
                    val pdfFiles = files.filter { file ->
                        file.extension.lowercase() == "pdf"
                    }
                    if (pdfFiles.isNotEmpty()) {
                        viewModel.handleIntent(SplitterIntent.AddPdfFile(pdfFiles.first()))
                    }
                }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.selectedFile == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "PDF File",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Drag & drop a PDF file here\nor click 'Select PDF' button",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Selected PDF",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = uiState.selectedFile!!.name,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Size: ${formatFileSize(uiState.selectedFile!!.length())}",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Total pages: ${uiState.totalPages.takeIf { it > 0 } ?: "Unknown"}",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Extracted Pages (${uiState.extractedPages.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(200.dp),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.extractedPages,
                            key = { it.id }
                        ) { page ->
                            val isNewPage = uiState.animatingNewPageIds.contains(page.id)

                            androidx.compose.animation.AnimatedVisibility(
                                visible = true,
                                enter = if (isNewPage) {
                                    scaleIn(
                                        initialScale = 0.3f,
                                        animationSpec = tween(600)
                                    ) + fadeIn(animationSpec = tween(600))
                                } else {
                                    scaleIn(initialScale = 1f, animationSpec = tween(0))
                                },
                                modifier = Modifier.animateItem(placementSpec = tween(300))
                            ) {
                                ExtractedPageCard(
                                    page = page,
                                    showReorderButtons = uiState.splitMode == SplitMode.MERGE_PAGES,
                                    canMoveLeft = uiState.extractedPages.indexOf(page) > 0,
                                    canMoveRight = uiState.extractedPages.indexOf(page) < uiState.extractedPages.size - 1,
                                    onDelete = { viewModel.handleIntent(SplitterIntent.StartDeleteAnimation(page)) },
                                    onMoveLeft = { viewModel.handleIntent(SplitterIntent.StartMoveAnimation(page, "left")) },
                                    onMoveRight = { viewModel.handleIntent(SplitterIntent.StartMoveAnimation(page, "right")) },
                                    isAnimatingDelete = uiState.animatingDeletePageId == page.id,
                                    isAnimatingMove = uiState.animatingMovePageId == page.id,
                                    moveDirection = uiState.moveDirection
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.splitMode == SplitMode.SAVE_ALL || uiState.extractedPages.isEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = uiState.outputFileName,
                    onValueChange = { viewModel.handleIntent(SplitterIntent.SetOutputFileName(it)) },
                    textStyle = TextStyle(
                        fontFamily = MaterialTheme.typography.labelMedium.fontFamily
                    ),
                    label = { Text("Output filename prefix", fontFamily = MaterialTheme.typography.labelSmall.fontFamily) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ){
                        Text(
                            text = "Output folder:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { viewModel.handleIntent(SplitterIntent.ShowFolderChooser) },
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Select folder",
                                modifier = Modifier.padding(end = 4.dp)
                            )
                            Text("Choose Folder", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (!uiState.outputFileDestination.isNullOrBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Text(
                                text = uiState.outputFileDestination!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.isSplitting || uiState.isSaving || uiState.isMerging,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val currentInfo = when {
                    uiState.isSplitting -> uiState.currentPageInfo
                    uiState.isSaving -> uiState.currentSaveInfo
                    uiState.isMerging -> uiState.currentMergeInfo
                    else -> null
                }

                val currentProgress = when {
                    uiState.isSplitting -> uiState.splitProgress
                    uiState.isSaving -> uiState.saveProgress
                    uiState.isMerging -> uiState.mergeProgress
                    else -> 0f
                }

                if (currentInfo != null) {
                    Text(
                        text = currentInfo,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${(currentProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        uiState.errorMessage?.let { message ->
            Card(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        uiState.successMessage?.let { message ->
            Card(
                border = BorderStroke(1.dp, Color(0xFF81C784)),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = message,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (uiState.selectedFile != null &&
            (uiState.splitMode == SplitMode.SAVE_ALL) &&
            uiState.outputFileDestination.isNullOrBlank()) {
            Card(
                border = BorderStroke(1.dp, Color(0xFFFF9800)), // Orange border
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFF9800).copy(alpha = 0.1f) // Light orange background
                )
            ) {
                Text(
                    text = "⚠️ Output folder must be selected by clicking on 'Choose Folder' button before splitting!",
                    color = Color(0xFFFF9800), // Orange text
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        ActionButtons(
            uiState = uiState,
            onSelectPdf = { viewModel.handleIntent(SplitterIntent.ShowFileChooser) },
            onClearAll = { viewModel.handleIntent(SplitterIntent.ClearAll) },
            onSplitPdf = { viewModel.handleIntent(SplitterIntent.SplitPdf) },
            onSavePages = { viewModel.handleIntent(SplitterIntent.SaveExtractedPages) },
            onMergeAndSave = { viewModel.handleIntent(SplitterIntent.MergeAndSavePages) }
        )
    }

    if (uiState.showFileChooser) {
        FileChooserDialog(
            title = "Select PDF File to Split",
            allowedExtensions = listOf("pdf"),
            onFileSelected = { file ->
                viewModel.handleIntent(SplitterIntent.AddPdfFile(file))
            },
            onCancel = {
                viewModel.handleIntent(SplitterIntent.HideFileChooser)
            }
        )
    }

    if (uiState.showFolderChooser) {
        FolderChooserDialog(
            title = "Select Output Directory",
            onFolderSelected = { folder ->
                viewModel.handleIntent(SplitterIntent.SelectOutputPath(folder.absolutePath))
                viewModel.handleIntent(SplitterIntent.HideFolderChooser)
            },
            onCancel = {
                viewModel.handleIntent(SplitterIntent.HideFolderChooser)
            }
        )
    }

    if (uiState.showSuccessDialog) {
        InfoDialog(
            title = "Success!",
            message = uiState.successMessage ?: "Operation completed successfully!",
            onClose = {
                viewModel.handleIntent(SplitterIntent.HideSuccessDialog)
            }
        )
    }

    if (uiState.showErrorDialog) {
        InfoDialog(
            title = "Error",
            message = uiState.errorMessage ?: "An error occurred.",
            onClose = {
                viewModel.handleIntent(SplitterIntent.HideErrorDialog)
            }
        )
    }
}

private fun formatFileSize(size: Long): String {
    if (size < 1024) return "$size B"

    val kilobytes = size / 1024.0
    if (kilobytes < 1024) return "%.2f KB".format(kilobytes)

    val megabytes = kilobytes / 1024.0
    return "%.2f MB".format(megabytes)
}