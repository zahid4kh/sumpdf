package splitter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun ActionButtons(
    uiState: SplitterViewModel.UiState,
    onSelectPdf: () -> Unit,
    onClearAll: () -> Unit,
    onSplitPdf: () -> Unit,
    onSavePages: () -> Unit,
    onMergeAndSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Button(
            onClick = onSelectPdf,
            enabled = !uiState.isSplitting && !uiState.isSaving && !uiState.isMerging,
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
            onClick = onClearAll,
            enabled = !uiState.isSplitting && !uiState.isSaving && !uiState.isMerging,
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

        when {
            uiState.extractedPages.isEmpty() -> {
                Button(
                    onClick = onSplitPdf,
                    enabled = uiState.selectedFile != null &&
                            (uiState.splitMode == SplitMode.SAVE_ALL && !uiState.outputFileDestination.isNullOrBlank() || uiState.splitMode != SplitMode.SAVE_ALL) &&
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
            uiState.splitMode == SplitMode.MERGE_PAGES -> {
                Button(
                    onClick = onMergeAndSave,
                    enabled = uiState.extractedPages.isNotEmpty() &&
                            !uiState.outputFileDestination.isNullOrBlank() &&
                            !uiState.isMerging,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isMerging) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (uiState.isMerging) "Merging..." else "Merge & Save",
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            else -> {
                Button(
                    onClick = onSavePages,
                    enabled = uiState.extractedPages.isNotEmpty() &&
                            !uiState.outputFileDestination.isNullOrBlank() &&
                            !uiState.isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onTertiary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (uiState.isSaving) "Saving..." else "Save Pages",
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}