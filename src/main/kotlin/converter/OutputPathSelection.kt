package converter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.awt.FileDialog
import java.awt.Frame
import java.io.File


@Composable
fun OutputPathSelection(
    selectedPath: String?,
    recentPaths: List<String>,
    onPathSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )

        Text(
            text = "Output folder:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(end = 12.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = {
                val dialog = FileDialog(Frame(), "Select Output Directory", FileDialog.SAVE)
                dialog.file = null
                dialog.isMultipleMode = false
                dialog.isVisible = true

                if (dialog.directory != null) {
                    val selectedDir = dialog.directory
                    onPathSelected(selectedDir)
                }
            },
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
            Text("Choose folder")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box {
            OutlinedButton(
                onClick = { expanded = true },
                enabled = recentPaths.isNotEmpty(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Recent folders",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Recent")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.width(IntrinsicSize.Max)
            ) {
                recentPaths.forEach { path ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = path,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        onClick = {
                            onPathSelected(path)
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (selectedPath != null) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = selectedPath.substring(selectedPath.lastIndexOf(File.separator) + 1),
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