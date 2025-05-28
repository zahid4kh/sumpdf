package converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import model.FileItem
import java.io.File

@Composable
fun FileList(
    files: List<FileItem>,
    onRemoveFile: (FileItem) -> Unit,
    onToggleSelection: (FileItem) -> Unit
) {
    if (files.isEmpty()) {
        EmptyFileList()
    } else {
        FilledFileList(files, onRemoveFile, onToggleSelection)
    }
}

@Composable
private fun EmptyFileList() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Drag & Drop files here or use the file picker",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilledFileList(
    files: List<FileItem>,
    onRemoveFile: (FileItem) -> Unit,
    onToggleSelection: (FileItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(files) { fileItem ->
            FileListItem(
                fileItem = fileItem,
                onRemoveFile = { onRemoveFile(fileItem) },
                onToggleSelection = { onToggleSelection(fileItem) }
            )
        }
    }
}

@Composable
private fun FileListItem(
    fileItem: FileItem,
    onRemoveFile: () -> Unit,
    onToggleSelection: () -> Unit
) {
    val file = fileItem.file

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleSelection) {
                Icon(
                    imageVector = if (fileItem.isSelected)
                        Icons.Default.CheckCircle
                    else
                        Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Select file",
                    tint = if (fileItem.isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = "File icon",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(end = 8.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = getFileTypeAndSize(file),
                    fontFamily = MaterialTheme.typography.labelSmall.fontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onRemoveFile) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove file",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun getFileTypeAndSize(file: File): String {
    val extension = file.extension.uppercase()
    val size = formatFileSize(file.length())

    return "$extension • $size"
}

private fun formatFileSize(size: Long): String {
    if (size < 1024) return "$size B"

    val kilobytes = size / 1024.0
    if (kilobytes < 1024) return "%.2f KB".format(kilobytes)

    val megabytes = kilobytes / 1024.0
    return "%.2f MB".format(megabytes)
}