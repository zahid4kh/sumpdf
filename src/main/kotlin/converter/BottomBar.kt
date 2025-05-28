package converter

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp


@Composable
fun BottomBar(
    fileCount: Int,
    isConverting: Boolean,
    conversionProgress: Float,
    onAddFiles: () -> Unit,
    onClearFiles: () -> Unit,
    onConvertFiles: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(
            visible = isConverting,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { conversionProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                color = MaterialTheme.colorScheme.primary
            )
        }

        BottomAppBar(
            modifier = Modifier.shadow(8.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            actions = {
                Button(
                    onClick = onAddFiles,
                    enabled = !isConverting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    ),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add files",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Add Files")
                }

                val clearButtonAlpha = animateFloatAsState(
                    targetValue = if (fileCount > 0 && !isConverting) 1f else 0.5f,
                    label = "Clear button alpha"
                )

                Button(
                    onClick = onClearFiles,
                    enabled = fileCount > 0 && !isConverting,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 0.dp
                    ),
                    modifier = Modifier.alpha(clearButtonAlpha.value)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear files",
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("Clear All")
                }
            },
            floatingActionButton = {
                val isFabEnabled = fileCount > 0 && !isConverting
                ExtendedFloatingActionButton(
                    text = {
                        Text(
                            text = "Convert (${fileCount})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Convert to PDF"
                        )
                    },
                    onClick = onConvertFiles,
                    expanded = fileCount > 0,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    ),
                    modifier = Modifier.alpha(if (isFabEnabled) 1f else 0.6f)
                )
            }
        )
    }
}