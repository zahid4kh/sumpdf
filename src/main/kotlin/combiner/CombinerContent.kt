package combiner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import deskit.dialogs.InfoDialog
import deskit.dialogs.file.FileChooserDialog
import deskit.dialogs.file.FileSaverDialog
import kotlinx.coroutines.delay

@Composable
fun CombinerContent(
    viewModel: CombinerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val pdfState by viewModel.state.collectAsState()

    pdfState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            delay(5000)
            viewModel.handleIntent(PdfCombinerIntent.ClearMessages)
        }
    }

    pdfState.successMessage?.let { message ->
        LaunchedEffect(message) {
            delay(5000)
            viewModel.handleIntent(PdfCombinerIntent.ClearMessages)
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
            text = "Select PDF files to combine them into a single document",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        PdfListArea(
            pdfFiles = pdfState.pdfFiles,
            onRemovePdf = { viewModel.handleIntent(PdfCombinerIntent.RemovePdf(it)) },
            onFilesDropped = { files ->
                files.forEach { file ->
                    viewModel.handleIntent(PdfCombinerIntent.AddPdf(file))
                }
            },
            onReorderPdfs = { fromIndex, toIndex ->
                viewModel.handleIntent(PdfCombinerIntent.ReorderPdfs(fromIndex, toIndex))
            },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = pdfState.outputFileName,
            onValueChange = { viewModel.handleIntent(PdfCombinerIntent.SetOutputFileName(it)) },
            textStyle = TextStyle(
                fontFamily = MaterialTheme.typography.labelMedium.fontFamily
            ),
            label = { Text("Output filename", fontFamily = MaterialTheme.typography.labelSmall.fontFamily) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = MaterialTheme.shapes.large,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        pdfState.errorMessage?.let { message ->
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

        pdfState.successMessage?.let { message ->
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Button(
                onClick = { viewModel.handleIntent(PdfCombinerIntent.ShowFileChooser) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add PDFs",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { viewModel.handleIntent(PdfCombinerIntent.ClearAll) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Clear All", fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.handleIntent(PdfCombinerIntent.ShowFileSaver) },
                enabled = pdfState.pdfFiles.isNotEmpty() && !pdfState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (pdfState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (pdfState.isLoading) "Combining..." else "Combine PDFs",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (uiState.showFileChooser) {
        FileChooserDialog(
            title = "Select PDF File",
            allowedExtensions = listOf("pdf"),
            onFileSelected = { file ->
                viewModel.handleIntent(PdfCombinerIntent.AddPdf(file))
            },
            onCancel = {
                viewModel.handleIntent(PdfCombinerIntent.HideFileChooser)
            }
        )
    }

    if (uiState.showFileSaver) {
        FileSaverDialog(
            title = "Save Combined PDF",
            suggestedFileName = pdfState.outputFileName,
            extension = ".pdf",
            onSave = { file ->
                viewModel.onSaveFileSelected(file)
            },
            onCancel = {
                viewModel.handleIntent(PdfCombinerIntent.HideFileSaver)
            }
        )
    }

    if (uiState.showSuccessDialog) {
        InfoDialog(
            title = "Success!",
            message = pdfState.successMessage ?: "PDFs combined successfully!",
            onClose = {
                viewModel.handleIntent(PdfCombinerIntent.HideSuccessDialog)
            }
        )
    }

    if (uiState.showErrorDialog) {
        InfoDialog(
            title = "Error",
            message = pdfState.errorMessage ?: "An error occurred.",
            onClose = {
                viewModel.handleIntent(PdfCombinerIntent.HideErrorDialog)
            }
        )
    }
}