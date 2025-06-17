package splitter

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

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

        AnimatedVisibility(
            visible = uiState.isSplitting,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.currentPageInfo != null) {
                    Text(
                        text = uiState.currentPageInfo!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                LinearProgressIndicator(
                    progress = { uiState.splitProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "${(uiState.splitProgress * 100).toInt()}%",
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

        //Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Button(
                onClick = { viewModel.handleIntent(SplitterIntent.ShowFileChooser) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Select PDF",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.handleIntent(SplitterIntent.ClearAll) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Clear All",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.handleIntent(SplitterIntent.SplitPdf) },
                enabled = uiState.selectedFile != null &&
                        !uiState.outputFileDestination.isNullOrBlank() &&
                        !uiState.isSplitting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (uiState.isSplitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (uiState.isSplitting) "Splitting..." else "Split PDF",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
            message = uiState.successMessage ?: "PDF split successfully!",
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