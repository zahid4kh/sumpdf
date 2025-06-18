package splitter

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ExtractedPageCard(
    page: ExtractedPage,
    showReorderButtons: Boolean,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    onDelete: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    isAnimatingDelete: Boolean = false,
    isAnimatingMove: Boolean = false,
    moveDirection: String? = null
) {
    var isHovered by remember { mutableStateOf(false) }
    val density = LocalDensity.current.density

    LaunchedEffect(Unit){
        println("density: $density")
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isAnimatingDelete -> 0f
            isAnimatingMove && moveDirection == "left" -> 1.1f
            isAnimatingMove && moveDirection == "right" -> 0.7f
            else -> 1f
        },
        animationSpec = tween(300),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isAnimatingDelete) 0f else 1f,
        animationSpec = tween(300),
        label = "alpha"
    )

    val offsetX by animateFloatAsState(
        targetValue = when {
            isAnimatingMove && moveDirection == "left" -> -230f
            isAnimatingMove && moveDirection == "right" -> 230f
            else -> 0f
        },
        animationSpec = tween(400),
        label = "offsetX"
    )


    Card(
        modifier = Modifier
            .width(170.dp)
            .height(170.dp)
            .offset(x = offsetX.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
            .pointerHoverIcon(PointerIcon(Cursor(Cursor.HAND_CURSOR)))
            .onPointerEvent(PointerEventType.Enter) {
                isHovered = true
            }
            .onPointerEvent(PointerEventType.Exit) {
                isHovered = false
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "PDF Page",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = page.fileName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "Size: ${formatFileSize(page.size)}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .size(24.dp)
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showReorderButtons && isHovered,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
                modifier = Modifier.align(Alignment.TopStart)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (canMoveLeft) {
                        IconButton(
                            onClick = onMoveLeft,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowLeft,
                                contentDescription = "Move Left",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    if (canMoveRight) {
                        IconButton(
                            onClick = onMoveRight,
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                contentDescription = "Move Right",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    if (size < 1024) return "$size B"

    val kilobytes = size / 1024.0
    if (kilobytes < 1024) return "%.2f KB".format(kilobytes)

    val megabytes = kilobytes / 1024.0
    return "%.2f MB".format(megabytes)
}