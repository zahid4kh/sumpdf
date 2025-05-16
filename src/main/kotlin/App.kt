import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import theme.AppTheme


@Composable
@Preview
fun App(
    viewModel: MainViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val pdfState by viewModel.state.collectAsState()

    pdfState.errorMessage?.let { message ->
        LaunchedEffect(message) {
            delay(5000)
            viewModel.handleIntent(PdfCombinerIntent.ClearMessages)
        }
    }

    pdfState.successMessage?.let { message ->
        LaunchedEffect(message) {
            delay(5000)
            viewModel.handleIntent(PdfCombinerIntent.ClearMessages)
        }
    }

    AppTheme(darkTheme = uiState.darkMode) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "PDF Combiner",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Select PDF files to combine them into a single document",
                fontSize = 14.sp,
                color = Color(0xFF666666),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            PdfListArea(
                pdfFiles = pdfState.pdfFiles,
                onRemovePdf = { viewModel.handleIntent(PdfCombinerIntent.RemovePdf(it)) },
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = pdfState.outputFileName,
                onValueChange = { viewModel.handleIntent(PdfCombinerIntent.SetOutputFileName(it)) },
                label = { Text("Output filename") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MaterialTheme.shapes.large
            )

            pdfState.errorMessage?.let { message ->
                Card(
                    border = BorderStroke(1.dp, Color(0xFFE57373))
                ) {
                    Text(
                        text = message,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            pdfState.successMessage?.let { message ->
                Card(
                    border = BorderStroke(1.dp, Color(0xFF81C784))
                ) {
                    Text(
                        text = message,
                        color = Color(0xFF388E3C),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Button(
                    onClick = { viewModel.handleIntent(PdfCombinerIntent.AddPdfs) },
                    modifier = Modifier.height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4A90E2),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Add PDFs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { viewModel.handleIntent(PdfCombinerIntent.ClearAll) },
                    modifier = Modifier.height(45.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Clear All", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { viewModel.handleIntent(PdfCombinerIntent.CombinePdfs) },
                    modifier = Modifier.height(45.dp),
                    enabled = pdfState.pdfFiles.isNotEmpty() && !pdfState.isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFCCCCCC),
                        disabledContentColor = Color(0xFF666666)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (pdfState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (pdfState.isLoading) "Combining..." else "Combine PDFs",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

