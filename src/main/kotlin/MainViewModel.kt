import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.apache.pdfbox.multipdf.PDFMergerUtility
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

class MainViewModel(
    private val database: Database,
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            val settings = database.getSettings()
            _uiState.value = _uiState.value.copy(
                darkMode = settings.darkMode,
            )
        }
    }

    private val _state = MutableStateFlow(PdfCombinerState())
    val state: StateFlow<PdfCombinerState> = _state.asStateFlow()

    private fun addPdfs() {
        val fileChooser = JFileChooser().apply {
            isMultiSelectionEnabled = true
            fileFilter = FileNameExtensionFilter("PDF files", "pdf")
        }

        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            val selectedFiles = fileChooser.selectedFiles
            val newPdfFiles = selectedFiles.map { PdfFile.from(it) }
            _state.value = _state.value.copy(
                pdfFiles = _state.value.pdfFiles + newPdfFiles,
                errorMessage = null
            )
        }
    }

    private fun combinePdfs() {
        if (_state.value.pdfFiles.isEmpty()) {
            _state.value = _state.value.copy(errorMessage = "Please add at least one PDF file.")
            return
        }

        _state.value = _state.value.copy(isLoading = true, errorMessage = null)

        try {
            val fileChooser = JFileChooser().apply {
                selectedFile = File("${_state.value.outputFileName}.pdf")
                fileFilter = FileNameExtensionFilter("PDF files", "pdf")
            }

            val result = fileChooser.showSaveDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val outputFile = fileChooser.selectedFile
                val merger = PDFMergerUtility()

                _state.value.pdfFiles.forEach { pdfFile ->
                    merger.addSource(File(pdfFile.path))
                }

                merger.destinationFileName = outputFile.absolutePath
                merger.mergeDocuments(null)

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "PDFs combined successfully!\nSaved to: ${outputFile.absolutePath}"
                )
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        } catch (e: Exception) {
            _state.value = _state.value.copy(
                isLoading = false,
                errorMessage = "Failed to combine PDFs: ${e.message}"
            )
        }
    }

    private fun removePdf(pdfFile: PdfFile) {
        _state.value = _state.value.copy(
            pdfFiles = _state.value.pdfFiles.filter { it.id != pdfFile.id }
        )
    }

    private fun clearAll() {
        _state.value = _state.value.copy(pdfFiles = emptyList())
    }

    private fun setOutputFileName(name: String) {
        _state.value = _state.value.copy(outputFileName = name)
    }

    private fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)

        scope.launch {
            val settings = database.getSettings()
            database.saveSettings(settings.copy(darkMode = newDarkMode))
        }
    }

    data class UiState(
        val darkMode: Boolean = false,
        val isConverting: Boolean = false
    )
}