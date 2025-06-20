package combiner

import Database
import com.vdurmont.semver4j.Semver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import sumpdf.BuildConfig
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CombinerViewModel(
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

    fun handleIntent(intent: PdfCombinerIntent) {
        when (intent) {
            is PdfCombinerIntent.ShowFileChooser -> showFileChooser()
            is PdfCombinerIntent.HideFileChooser -> hideFileChooser()
            is PdfCombinerIntent.AddPdf -> addPdf(intent.file)
            is PdfCombinerIntent.RemovePdf -> removePdf(intent.pdfFile)
            is PdfCombinerIntent.ClearAll -> clearAll()
            is PdfCombinerIntent.SetOutputFileName -> setOutputFileName(intent.name)
            is PdfCombinerIntent.ShowFileSaver -> showFileSaver()
            is PdfCombinerIntent.HideFileSaver -> hideFileSaver()
            is PdfCombinerIntent.CombinePdfs -> combinePdfs()
            is PdfCombinerIntent.ClearMessages -> clearMessages()
            is PdfCombinerIntent.ShowSuccessDialog -> showSuccessDialog()
            is PdfCombinerIntent.HideSuccessDialog -> hideSuccessDialog()
            is PdfCombinerIntent.ShowErrorDialog -> showErrorDialog()
            is PdfCombinerIntent.HideErrorDialog -> hideErrorDialog()
            is PdfCombinerIntent.ReorderPdfs -> reorderPdfs(intent.fromIndex, intent.toIndex)
            is PdfCombinerIntent.CheckForUpdates -> checkForUpdates()
        }
    }

    private fun showFileChooser() {
        log("(showFileChooser) Opening file chooser dialog")
        _uiState.value = _uiState.value.copy(showFileChooser = true)
    }

    private fun hideFileChooser() {
        log("(hideFileChooser) Closing file chooser dialog")
        _uiState.value = _uiState.value.copy(showFileChooser = false)
    }

    private fun addPdf(file: File) {
        if (file.extension.lowercase() == "pdf") {
            val newPdfFile = PdfFile.from(file)
            log("(addPdf) Added PDF: ${file.name}")
            _state.value = _state.value.copy(
                pdfFiles = _state.value.pdfFiles + newPdfFile,
                errorMessage = null
            )
        } else {
            log("(addPdf) Error: Invalid file type - ${file.name}")
            _state.value = _state.value.copy(
                errorMessage = "Please select a valid PDF file."
            )
            _uiState.value = _uiState.value.copy(showErrorDialog = true)
        }
        hideFileChooser()
    }

    private fun showFileSaver() {
        if (_state.value.pdfFiles.isEmpty()) {
            log("(showFileSaver) Error: No PDF files to save")
            _state.value = _state.value.copy(errorMessage = "Please add at least one PDF file.")
            _uiState.value = _uiState.value.copy(showErrorDialog = true)
            return
        }
        log("(showFileSaver) Opening file saver dialog")
        _uiState.value = _uiState.value.copy(showFileSaver = true)
    }

    private fun hideFileSaver() {
        log("(hideFileSaver) Closing file saver dialog")
        _uiState.value = _uiState.value.copy(showFileSaver = false)
    }

    private fun combinePdfs() {
        _state.value = _state.value.copy(isLoading = true, errorMessage = null)
        log("(combinePdfs) Starting combine operation with ${_state.value.pdfFiles.size} PDFs")

        scope.launch {
            try {
                val outputFile = _uiState.value.selectedSaveFile
                if (outputFile != null) {
                    val merger = PDFMergerUtility()

                    _state.value.pdfFiles.forEach { pdfFile ->
                        log("(combinePdfs) Adding to merger: ${File(pdfFile.path).name}")
                        merger.addSource(File(pdfFile.path))
                    }

                    merger.destinationFileName = outputFile.absolutePath
                    merger.mergeDocuments(null, CompressParameters(500))

                    log("(combinePdfs) Successfully combined PDFs to: ${outputFile.name}")
                    _state.value = _state.value.copy(
                        isLoading = false,
                        successMessage = "PDFs combined successfully!\nSaved to: ${outputFile.absolutePath}"
                    )
                    _uiState.value = _uiState.value.copy(
                        showSuccessDialog = true,
                        selectedSaveFile = null
                    )
                } else {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to combine PDFs: ${e.message}"
                )
                _uiState.value = _uiState.value.copy(showErrorDialog = true)
            }
        }
        hideFileSaver()
    }

    private fun removePdf(pdfFile: PdfFile) {
        log("(removePdf) Removed PDF: ${File(pdfFile.path).name}")
        _state.value = _state.value.copy(
            pdfFiles = _state.value.pdfFiles.filter { it.id != pdfFile.id }
        )
    }

    private fun clearAll() {
        log("(clearAll) Clearing all PDF files (${_state.value.pdfFiles.size} files)")
        _state.value = _state.value.copy(pdfFiles = emptyList())
    }

    private fun reorderPdfs(fromIndex: Int, toIndex: Int) {
        val currentList = _state.value.pdfFiles.toMutableList()
        if (fromIndex in currentList.indices && toIndex in currentList.indices) {
            val item = currentList.removeAt(fromIndex)
            currentList.add(toIndex, item)
            log("(reorderPdfs) Reordered PDF from index $fromIndex to $toIndex")
            _state.value = _state.value.copy(pdfFiles = currentList)
        }
    }

    private fun setOutputFileName(name: String) {
        log("(setOutputFileName) Output filename set to: $name")
        _state.value = _state.value.copy(outputFileName = name)
    }

    private fun clearMessages() {
        log("(clearMessages) Clearing error and success messages")
        _state.value = _state.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun showSuccessDialog() {
        log("(showSuccessDialog) Showing success dialog")
        _uiState.value = _uiState.value.copy(showSuccessDialog = true)
    }

    private fun hideSuccessDialog() {
        log("(hideSuccessDialog) Hiding success dialog and clearing messages")
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
        clearMessages()
    }

    private fun showErrorDialog() {
        log("(showErrorDialog) Showing error dialog")
        _uiState.value = _uiState.value.copy(showErrorDialog = true)
    }

    private fun hideErrorDialog() {
        log("(hideErrorDialog) Hiding error dialog and clearing messages")
        _uiState.value = _uiState.value.copy(showErrorDialog = false)
        clearMessages()
    }

    fun onSaveFileSelected(file: File) {
        log("(onSaveFileSelected) File selected for saving: ${file.name}")
        _uiState.value = _uiState.value.copy(selectedSaveFile = file)
        handleIntent(PdfCombinerIntent.CombinePdfs)
    }

    fun toggleDarkMode() {
        val newDarkMode = !_uiState.value.darkMode
        log("(toggleDarkMode) Switching to ${if (newDarkMode) "dark" else "light"} mode")
        _uiState.value = _uiState.value.copy(darkMode = newDarkMode)

        scope.launch {
            val settings = database.getSettings()
            database.saveSettings(settings.copy(darkMode = newDarkMode))
        }
    }

    private fun checkForUpdates() {
        log("(checkForUpdates) Starting update check")
        _uiState.value = _uiState.value.copy(
            isCheckingUpdates = true,
            showNewUpdatesDialog = true,
            updateMessage = null,
            isUpdateAvailable = false
        )

        scope.launch(Dispatchers.IO) {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.github.com/repos/zahid4kh/sumpdf/tags")
                .build()

            try {
                log("(checkForUpdates) Making API request to GitHub")
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        log("(checkForUpdates) API request failed with HTTP ${response.code}")
                        scope.launch(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                isCheckingUpdates = false,
                                updateMessage = "Error fetching updates: HTTP ${response.code}",
                                isUpdateAvailable = false
                            )
                        }
                        return@use
                    }

                    val responseBody = response.body?.string() ?: return@use
                    val json = Json.parseToJsonElement(responseBody).jsonArray

                    if (json.isNotEmpty()) {
                        val latestTag = json[0].jsonObject["name"]?.jsonPrimitive?.content ?: return@use
                        val current = Semver(BuildConfig.VERSION_NAME)
                        val latest = Semver(latestTag.replace("^v", ""))
                        scope.launch(Dispatchers.Main) {
                            if (latest.isGreaterThan(current)) {
                                log("(checkForUpdates) New version available: $latestTag (current: ${BuildConfig.VERSION_NAME})")
                                _uiState.value = _uiState.value.copy(
                                    isCheckingUpdates = false,
                                    updateMessage = "New version available: $latestTag. Download the latest version from GitHub.",
                                    isUpdateAvailable = true
                                )
                            } else {
                                log("(checkForUpdates) Already using latest version: ${BuildConfig.VERSION_NAME}")
                                _uiState.value = _uiState.value.copy(
                                    isCheckingUpdates = false,
                                    updateMessage = "You are using the latest version: ${BuildConfig.VERSION_NAME}",
                                    isUpdateAvailable = false
                                )
                            }
                        }
                    } else {
                        log("(checkForUpdates) No version tags found in repository")
                        scope.launch(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                isCheckingUpdates = false,
                                updateMessage = "No version tags found in the repository.",
                                isUpdateAvailable = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                log("(checkForUpdates) Error during update check: ${e.message}")
                scope.launch(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdates = false,
                        updateMessage = "Error checking for updates: ${e.message}",
                        isUpdateAvailable = false
                    )
                }
            }
        }
    }

    fun hideNewUpdatesDialog() {
        log("(hideNewUpdatesDialog) Hiding update dialog")
        _uiState.value = _uiState.value.copy(
            showNewUpdatesDialog = false,
            isCheckingUpdates = false,
            updateMessage = null,
            isUpdateAvailable = false
        )
    }

    private fun log(message: String) {
        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
        )
        println("[$timestamp] [${this::class.simpleName}] $message")
    }

    data class UiState(
        val darkMode: Boolean = false,
        val isConverting: Boolean = false,
        val showFileChooser: Boolean = false,
        val showFileSaver: Boolean = false,
        val showSuccessDialog: Boolean = false,
        val showErrorDialog: Boolean = false,
        val selectedSaveFile: File? = null,
        val isCheckingUpdates: Boolean = false,
        val showNewUpdatesDialog: Boolean = false,
        val updateMessage: String? = null,
        val isUpdateAvailable: Boolean = false
    )
}