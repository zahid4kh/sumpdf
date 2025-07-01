import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import combiner.CombinerViewModel
import combiner.PdfCombinerIntent
import deskit.dialogs.info.InfoDialog
import org.jetbrains.compose.resources.painterResource
import sumpdf.BuildConfig
import sumpdf.resources.Res
import sumpdf.resources.combine_svgrepo_com
import java.awt.Desktop
import java.net.URI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScreen(
    darkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateToCombiner: () -> Unit,
    onNavigateToConverter: () -> Unit,
    onNavigateToSplitter: () -> Unit,
    onNavigateToSelectiveSplitter: () -> Unit,
    viewModel: CombinerViewModel
) {
    val uiState = viewModel.uiState.collectAsState()
    val cardHeight by remember { mutableStateOf(300.dp) }
    var showAppInfo by remember { mutableStateOf(false)}

    val isLinux = System.getProperty("os.name").lowercase() == "linux"
    val isWindows = System.getProperty("os.name").lowercase() == "windows"

    if (showAppInfo) {
        InfoDialog(
            width = 450.dp,
            height = 370.dp,
            title = "About SumPDF",
            iconTint = MaterialTheme.colorScheme.onSurface,
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
                        text = "Version: ${BuildConfig.VERSION_NAME}",
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

    if (uiState.value.showNewUpdatesDialog) {
        InfoDialog(
            width = 450.dp,
            height = 320.dp,
            title = "Check for Updates",
            iconTint = MaterialTheme.colorScheme.onSurface,
            onClose = { viewModel.hideNewUpdatesDialog() },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.value.isCheckingUpdates) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Checking for updates...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    } else {
                        Text(
                            text = uiState.value.updateMessage ?: "No update information available.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                        if (uiState.value.isUpdateAvailable) {
                            val downloadUrl = "https://sumpdf.vercel.app"
                            if (isWindows) {
                                Text(
                                    text = "Download the latest .exe or .msi installer from:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = downloadUrl,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            Desktop.getDesktop().browse(URI(downloadUrl))
                                        }
                                        .pointerHoverIcon(icon = PointerIcon.Hand)
                                )
                            } else if (isLinux) {
                                Text(
                                    text = "Download and install the latest Debian package from:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = downloadUrl,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            Desktop.getDesktop().browse(URI(downloadUrl))
                                        }
                                        .pointerHoverIcon(icon = PointerIcon.Hand)
                                )
                                Text(
                                    text = "Alternatively, if you installed SumPDF via APT repository, you can update by running:\n'sudo apt update && sudo apt upgrade'",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
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

                    IconButton(
                        onClick = {viewModel.handleIntent(PdfCombinerIntent.CheckForUpdates)}
                    ){
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Check for updates"
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

            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(300.dp),
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalItemSpacing = 16.dp,
                content = {
                    item {
                        OutlinedCard(
                            modifier = Modifier
                                .height(cardHeight)
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .animateItem(placementSpec = spring()),
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
                    }

                    item {
                        OutlinedCard(
                            modifier = Modifier
                                .height(cardHeight)
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .animateItem(placementSpec = spring()),
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

                    item {
                        OutlinedCard(
                            modifier = Modifier
                                .height(cardHeight)
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .animateItem(placementSpec = spring()),
                            shape = RoundedCornerShape(16.dp),
                            onClick = onNavigateToSplitter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Splitscreen,
                                    contentDescription = "Split PDF",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Split PDF",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Split PDF into individual pages",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        OutlinedCard(
                            modifier = Modifier
                                .height(cardHeight)
                                .pointerHoverIcon(icon = PointerIcon.Hand)
                                .animateItem(placementSpec = spring()),
                            shape = RoundedCornerShape(16.dp),
                            onClick = onNavigateToSelectiveSplitter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ViewArray,
                                    contentDescription = "Split Range",
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Split Range",
                                    style = MaterialTheme.typography.titleLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Extract specific page range from PDF",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            )
        }
    }
}