package converter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(
    viewModel: ConverterViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showResultDialog) {
        ConversionResultDialog(
            results = uiState.conversionResults,
            onDismiss = viewModel::dismissResultDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Convert to PDF") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            DragDropArea(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                onFilesDropped = { files ->
                    val validFiles = files.filter { file ->
                        val ext = file.extension.lowercase()
                        ext in listOf("txt", "odt", "png", "jpg", "jpeg", "svg")
                    }
                    viewModel.addFiles(validFiles)
                }
            ) {
                FileList(
                    files = uiState.files,
                    onRemoveFile = viewModel::removeFile,
                    onToggleSelection = viewModel::toggleFileSelection
                )
            }

            OutputPathSelection(
                selectedPath = uiState.selectedOutputPath,
                recentPaths = uiState.recentOutputPaths,
                onPathSelected = viewModel::selectOutputPath
            )

            BottomBar(
                fileCount = uiState.files.size,
                isConverting = uiState.isConverting,
                conversionProgress = uiState.conversionProgress,
                onAddFiles = {
                    openFileDialog(
                        lastUsedDirectory = uiState.lastUsedDirectory,
                        onFilesSelected = { files, directory ->
                            viewModel.addFiles(files)
                            viewModel.setLastUsedDirectory(directory)
                        }
                    )
                },
                onClearFiles = viewModel::clearFiles,
                onConvertFiles = viewModel::convertFiles
            )
        }
    }
}

private fun openFileDialog(
    lastUsedDirectory: String?,
    onFilesSelected: (List<File>, String) -> Unit
) {
    val dialog = FileDialog(Frame(), "Select Files to Convert", FileDialog.LOAD)
    dialog.isMultipleMode = true

    if (lastUsedDirectory != null) {
        dialog.directory = lastUsedDirectory
    }

    dialog.filenameFilter = FilenameFilter { _, name ->
        val ext = name.substringAfterLast('.', "").lowercase()
        ext in listOf("txt", "odt", "png", "jpg", "jpeg", "svg")
    }

    dialog.isVisible = true

    val files = dialog.files.toList()
    if (files.isNotEmpty() && dialog.directory != null) {
        onFilesSelected(files, dialog.directory)
    }
}