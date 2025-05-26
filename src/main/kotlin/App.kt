import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import theme.AppTheme
import dialogs.InfoDialog
import dialogs.file.FileChooserDialog
import dialogs.file.FileSaverDialog

@Composable
@Preview
fun App(
    viewModel: MainViewModel
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

    AppTheme(darkTheme = uiState.darkMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "PDF Combiner",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Start
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Dark Mode",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                    Switch(
                        checked = uiState.darkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }

            Text(
                text = "Select PDF files to combine them into a single document",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            PdfListArea(
                pdfFiles = pdfState.pdfFiles,
                onRemovePdf = { viewModel.handleIntent(PdfCombinerIntent.RemovePdf(it)) },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = pdfState.outputFileName,
                onValueChange = { viewModel.handleIntent(PdfCombinerIntent.SetOutputFileName(it)) },
                label = { Text("Output filename") },
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
                    modifier = Modifier.height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add PDFs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.handleIntent(PdfCombinerIntent.ClearAll) },
                    modifier = Modifier.height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.handleIntent(PdfCombinerIntent.ShowFileSaver) },
                    modifier = Modifier.height(45.dp),
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
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