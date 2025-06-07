package converter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import deskit.dialogs.file.FileChooserDialog
import deskit.dialogs.file.FolderChooserDialog
import java.io.File

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

    if (uiState.showFileChooser) {
        FileChooserDialog(
            title = "Select Files to Convert",
            allowedExtensions = listOf("txt", "odt", "doc", "docx", "png", "jpg", "jpeg", "svg"),
            startDirectory = File(uiState.lastUsedDirectory?: System.getProperty("user.home")),
            onFileSelected = { file ->
                viewModel.addFiles(listOf(file))
                viewModel.setLastUsedDirectory(file.parentFile.absolutePath)
                viewModel.hideFileChooser()
            },
            onCancel = {
                viewModel.hideFileChooser()
            }
        )
    }

    if (uiState.showFolderChooser) {
        FolderChooserDialog(
            title = "Select Output Directory",
            startDirectory = File(uiState.lastUsedDirectory?: System.getProperty("user.home")),
            onFolderSelected = { folder ->
                viewModel.selectOutputPath(folder.absolutePath)
                viewModel.hideFolderChooser()
            },
            onCancel = {
                viewModel.hideFolderChooser()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Convert to PDF", fontFamily = MaterialTheme.typography.titleLarge.fontFamily) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.clip(
                    RoundedCornerShape(bottomStart = 17.dp, bottomEnd = 17.dp)
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
                        ext in listOf("txt", "odt", "doc", "docx", "png", "jpg", "jpeg", "svg")
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
                onPathSelected = viewModel::selectOutputPath,
                onShowFolderChooser = viewModel::showFolderChooser
            )

            BottomBar(
                fileCount = uiState.files.size,
                isConverting = uiState.isConverting,
                conversionProgress = uiState.conversionProgress,
                currentlyConverting = uiState.currentlyConverting,
                onAddFiles = viewModel::showFileChooser,
                onClearFiles = viewModel::clearFiles,
                onConvertFiles = viewModel::convertFiles
            )
        }
    }
}