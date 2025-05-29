package converter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dialogs.InfoDialog
import model.ConversionResult
import org.apache.batik.svggen.SVGCSSStyler.style
import org.jetbrains.compose.resources.painterResource
import sumpdf.resources.Res
import sumpdf.resources.combine_svgrepo_com
import sumpdf.resources.sumpdf
import java.io.File

@Composable
fun ConversionResultDialog(
    results: List<ConversionResult>,
    onDismiss: () -> Unit
) {
    val successful = results.count { it.success }
    val failed = results.size - successful

    InfoDialog(
        title = "Conversion Results",
        content = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Successfully converted: $successful | Failed: $failed",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { result ->
                        ResultItem(result)
                    }
                }
            }
        },
        onClose = onDismiss
    )
}

@Composable
private fun ResultItem(result: ConversionResult) {
    val task = result.task
    val inputFile = File(task.inputFilePath).name
    val outputFile = task.outputFilePath?.let { File(it).name } ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (result.success)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (task.status) {
                    ConversionStatus.COMPLETED -> Icons.Default.Check
                    ConversionStatus.FAILED -> Icons.Default.Error
                    else -> Icons.Default.Error
                },
                contentDescription = "Conversion status",
                tint = when (task.status) {
                    ConversionStatus.COMPLETED -> MaterialTheme.colorScheme.primary
                    ConversionStatus.FAILED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(end = 12.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = inputFile,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (result.success) {
                    Text(
                        text = "→ $outputFile",
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = result.error ?: "Unknown error",
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}