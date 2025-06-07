import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import deskit.dialogs.InfoDialog
import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
import org.apache.batik.svggen.SVGCSSStyler.style
import org.jetbrains.compose.resources.painterResource
import sumpdf.resources.Res
import sumpdf.resources.combine_svgrepo_com
import sumpdf.resources.sumpdf
import sumpdf.resources.vectorsumpdf
import java.awt.Desktop
import java.net.URI
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateToCombiner: () -> Unit,
    onNavigateToConverter: () -> Unit
) {
    val cardHeight by remember { mutableStateOf(370.dp) }
    var showAppInfo by remember { mutableStateOf(false)}

    if (showAppInfo) {
        InfoDialog(
            width = 450.dp,
            height = 370.dp,
            title = "About SumPDF",
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurface),
            onClose = { showAppInfo = false },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "SumPDF",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Version: 1.1.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Developer: zahid4kh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable{
                                Desktop.getDesktop().browse(URI("https://github.com/zahid4kh"))
                            }
                            .pointerHoverIcon(icon = PointerIcon.Hand)
                    )
                    Text(
                        text = "License: Apache 2.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SumPDF is a free and open-source desktop application that allows you to effortlessly combine multiple PDF files and convert various file types to PDF format.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(
                    "SumPDF",
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                ) },
                actions = {
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier
                            .pointerHoverIcon(icon = PointerIcon.Hand),
                    ) {
                        Icon(
                            imageVector = if (darkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkMode) "Switch to Light Mode" else "Switch to Dark Mode"
                        )
                    }

                    IconButton(
                        onClick = {showAppInfo = true},
                        modifier = Modifier
                            .pointerHoverIcon(icon = PointerIcon.Hand),
                    ){
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "App Info"
                        )
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
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Text(
                    text = "Welcome to SumPDF",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 50.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Text(
                    text = "Your all-in-one PDF tool",
                    style = MaterialTheme.typography.bodyLarge,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(cardHeight)
                        //.width(cardWidth)
                        .pointerHoverIcon(icon = PointerIcon.Hand),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onNavigateToCombiner
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.combine_svgrepo_com),
                            contentDescription = "Combine PDFs",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Combine PDFs",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Merge multiple PDF files into one",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                OutlinedCard(
                    modifier = Modifier
                        .weight(1f)
                        .height(cardHeight)
                        //.width(cardWidth)
                        .pointerHoverIcon(icon = PointerIcon.Hand),
                    shape = RoundedCornerShape(16.dp),
                    onClick = onNavigateToConverter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Transform,
                            contentDescription = "Convert to PDF",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Convert to PDF",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Convert images and documents to PDF",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}