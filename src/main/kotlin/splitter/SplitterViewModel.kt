package splitter

import Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.pdfbox.Loader
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import java.io.File
import java.io.IOException
import java.util.*

class SplitterViewModel(
    private val database: Database
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    fun handleIntent(intent: SplitterIntent) {
        when (intent) {
            is SplitterIntent.ShowFileChooser -> showFileChooser()
            is SplitterIntent.HideFileChooser -> hideFileChooser()
            is SplitterIntent.ShowFolderChooser -> showFolderChooser()
            is SplitterIntent.HideFolderChooser -> hideFolderChooser()
            is SplitterIntent.AddPdfFile -> addPdfFile(intent.file)
            is SplitterIntent.SelectOutputPath -> selectOutputPath(intent.path)
            is SplitterIntent.SetOutputFileName -> setOutputFileName(intent.name)
            is SplitterIntent.SplitPdf -> splitPdf()
            is SplitterIntent.ClearAll -> clearAll()
            is SplitterIntent.ClearMessages -> clearMessages()
            is SplitterIntent.ShowSuccessDialog -> showSuccessDialog()
            is SplitterIntent.HideSuccessDialog -> hideSuccessDialog()
            is SplitterIntent.ShowErrorDialog -> showErrorDialog()
            is SplitterIntent.HideErrorDialog -> hideErrorDialog()
            is SplitterIntent.SelectSplitMode -> selectSplitMode(intent.mode)
            is SplitterIntent.DeleteExtractedPage -> deleteExtractedPage(intent.page)
            is SplitterIntent.MovePageLeft -> movePageLeft(intent.page)
            is SplitterIntent.MovePageRight -> movePageRight(intent.page)
            is SplitterIntent.SaveExtractedPages -> saveExtractedPages()
            is SplitterIntent.MergeAndSavePages -> mergeAndSavePages()
        }
    }

    private fun splitPdf() {
        val selectedFile = _uiState.value.selectedFile
        val outputPath = _uiState.value.outputFileDestination
        val outputPrefix = _uiState.value.outputFileName
        val splitMode = _uiState.value.splitMode

        if (selectedFile == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a PDF file to split.",
                showErrorDialog = true
            )
            return
        }

        if (splitMode == SplitMode.SAVE_ALL && outputPath.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select an output directory.",
                showErrorDialog = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isSplitting = true,
            splitProgress = 0f,
            currentPageInfo = "Initializing...",
            errorMessage = null
        )

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val document: PDDocument = Loader.loadPDF(selectedFile)
                    val totalPages = document.numberOfPages

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            totalPages = totalPages,
                            currentPageInfo = "Splitting $totalPages pages..."
                        )
                    }

                    delay(300)

                    val splitter = Splitter()
                    val pages: List<PDDocument> = splitter.split(document)

                    if (splitMode == SplitMode.SAVE_ALL) {
                        val outputDir = File(outputPath!!)
                        if (!outputDir.exists()) {
                            outputDir.mkdirs()
                        }

                        var successCount = 0
                        var failureCount = 0

                        pages.forEachIndexed { index, pageDoc ->
                            try {
                                val pageNumber = index + 1
                                val fileName = "${outputPrefix}page_$pageNumber.pdf"
                                val outputFile = File(outputDir, fileName)

                                withContext(Dispatchers.Main) {
                                    _uiState.value = _uiState.value.copy(
                                        currentPageInfo = "Saving page $pageNumber of $totalPages...",
                                        splitProgress = (pageNumber.toFloat() / totalPages)
                                    )
                                }

                                pageDoc.save(outputFile.absolutePath)
                                pageDoc.close()
                                successCount++

                                delay(200)
                            } catch (e: IOException) {
                                failureCount++
                                println("Failed to save page ${index + 1}: ${e.message}")
                            }
                        }

                        document.close()

                        withContext(Dispatchers.Main) {
                            if (failureCount == 0) {
                                _uiState.value = _uiState.value.copy(
                                    isSplitting = false,
                                    splitProgress = 1f,
                                    successMessage = "Successfully split PDF into $successCount pages!\nSaved to: $outputPath",
                                    showSuccessDialog = true,
                                    currentPageInfo = null
                                )
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    isSplitting = false,
                                    errorMessage = "Split completed with errors. $successCount pages saved, $failureCount failed.",
                                    showErrorDialog = true,
                                    currentPageInfo = null
                                )
                            }
                        }
                    } else {
                        val extractedPages = pages.mapIndexed { index, pageDoc ->
                            val pageNumber = index + 1
                            val fileName = "${outputPrefix}page_$pageNumber.pdf"

                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    currentPageInfo = "Extracting page $pageNumber of $totalPages...",
                                    splitProgress = (pageNumber.toFloat() / totalPages)
                                )
                            }

                            delay(150)

                            ExtractedPage(
                                id = UUID.randomUUID().toString(),
                                pageNumber = pageNumber,
                                fileName = fileName,
                                document = pageDoc,
                                size = 50000L + (0..100000).random()
                            )
                        }

                        document.close()

                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                isSplitting = false,
                                splitProgress = 1f,
                                extractedPages = extractedPages,
                                currentPageInfo = null,
                                successMessage = "Successfully extracted $totalPages pages! Review and manage pages below."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSplitting = false,
                    errorMessage = "Failed to split PDF: ${e.message}",
                    showErrorDialog = true,
                    currentPageInfo = null,
                    splitProgress = 0f
                )
            }
        }
    }

    private fun saveExtractedPages() {
        val outputPath = _uiState.value.outputFileDestination
        val extractedPages = _uiState.value.extractedPages

        if (outputPath.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select an output directory.",
                showErrorDialog = true
            )
            return
        }

        if (extractedPages.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No pages to save.",
                showErrorDialog = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isSaving = true,
            saveProgress = 0f,
            currentSaveInfo = "Preparing to save pages..."
        )

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val outputDir = File(outputPath)
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    var successCount = 0
                    var failureCount = 0

                    extractedPages.forEachIndexed { index, page ->
                        try {
                            val outputFile = File(outputDir, page.fileName)

                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    currentSaveInfo = "Saving ${page.fileName} (${index + 1}/${extractedPages.size})...",
                                    saveProgress = ((index + 1).toFloat() / extractedPages.size)
                                )
                            }

                            page.document.save(outputFile.absolutePath)
                            page.document.close()
                            successCount++

                            delay(300)
                        } catch (e: IOException) {
                            failureCount++
                            println("Failed to save ${page.fileName}: ${e.message}")
                        }
                    }

                    withContext(Dispatchers.Main) {
                        if (failureCount == 0) {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                saveProgress = 1f,
                                successMessage = "Successfully saved $successCount pages!\nSaved to: $outputPath",
                                showSuccessDialog = true,
                                currentSaveInfo = null,
                                extractedPages = emptyList()
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isSaving = false,
                                errorMessage = "Save completed with errors. $successCount pages saved, $failureCount failed.",
                                showErrorDialog = true,
                                currentSaveInfo = null
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = "Failed to save pages: ${e.message}",
                    showErrorDialog = true,
                    currentSaveInfo = null,
                    saveProgress = 0f
                )
            }
        }
    }

    private fun mergeAndSavePages() {
        val outputPath = _uiState.value.outputFileDestination
        val extractedPages = _uiState.value.extractedPages
        val outputPrefix = _uiState.value.outputFileName

        if (outputPath.isNullOrBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select an output directory.",
                showErrorDialog = true
            )
            return
        }

        if (extractedPages.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No pages to merge.",
                showErrorDialog = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isMerging = true,
            mergeProgress = 0f,
            currentMergeInfo = "Preparing to merge pages..."
        )

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val outputDir = File(outputPath)
                    if (!outputDir.exists()) {
                        outputDir.mkdirs()
                    }

                    val mergedFileName = "${outputPrefix}merged.pdf"
                    val outputFile = File(outputDir, mergedFileName)

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            currentMergeInfo = "Merging ${extractedPages.size} pages...",
                            mergeProgress = 0.2f
                        )
                    }

                    delay(400)

                    val merger = PDFMergerUtility()
                    merger.destinationFileName = outputFile.absolutePath

                    extractedPages.forEachIndexed { index, page ->
                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                currentMergeInfo = "Adding page ${index + 1} of ${extractedPages.size}...",
                                mergeProgress = (0.2f + (index + 1).toFloat() / extractedPages.size * 0.6f)
                            )
                        }

                        val tempFile = File.createTempFile("temp_page_${page.pageNumber}", ".pdf")
                        page.document.save(tempFile.absolutePath)
                        merger.addSource(tempFile)

                        delay(200)
                    }

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            currentMergeInfo = "Finalizing merged document...",
                            mergeProgress = 0.9f
                        )
                    }

                    merger.mergeDocuments(null, CompressParameters(500))

                    extractedPages.forEach { it.document.close() }

                    delay(300)

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isMerging = false,
                            mergeProgress = 1f,
                            successMessage = "Successfully merged ${extractedPages.size} pages into $mergedFileName!\nSaved to: $outputPath",
                            showSuccessDialog = true,
                            currentMergeInfo = null,
                            extractedPages = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMerging = false,
                    errorMessage = "Failed to merge pages: ${e.message}",
                    showErrorDialog = true,
                    currentMergeInfo = null,
                    mergeProgress = 0f
                )
            }
        }
    }

    private fun selectSplitMode(mode: SplitMode) {
        _uiState.value = _uiState.value.copy(
            splitMode = mode,
            extractedPages = emptyList()
        )
    }

    private fun deleteExtractedPage(page: ExtractedPage) {
        val currentPages = _uiState.value.extractedPages.toMutableList()
        currentPages.remove(page)
        page.document.close()
        _uiState.value = _uiState.value.copy(extractedPages = currentPages)
    }

    private fun movePageLeft(page: ExtractedPage) {
        val currentPages = _uiState.value.extractedPages.toMutableList()
        val currentIndex = currentPages.indexOf(page)
        if (currentIndex > 0) {
            currentPages.removeAt(currentIndex)
            currentPages.add(currentIndex - 1, page)
            _uiState.value = _uiState.value.copy(extractedPages = currentPages)
        }
    }

    private fun movePageRight(page: ExtractedPage) {
        val currentPages = _uiState.value.extractedPages.toMutableList()
        val currentIndex = currentPages.indexOf(page)
        if (currentIndex < currentPages.size - 1) {
            currentPages.removeAt(currentIndex)
            currentPages.add(currentIndex + 1, page)
            _uiState.value = _uiState.value.copy(extractedPages = currentPages)
        }
    }

    private fun addPdfFile(file: File) {
        if (file.extension.lowercase() == "pdf") {
            _uiState.value = _uiState.value.copy(
                selectedFile = file,
                outputFileName = "${file.nameWithoutExtension}_",
                errorMessage = null,
                extractedPages = emptyList()
            )
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a valid PDF file."
            )
            showErrorDialog()
        }
        hideFileChooser()
    }

    private fun selectOutputPath(path: String) {
        _uiState.value = _uiState.value.copy(outputFileDestination = path)
    }

    private fun setOutputFileName(name: String) {
        _uiState.value = _uiState.value.copy(outputFileName = name)
    }

    private fun clearAll() {
        _uiState.value.extractedPages.forEach { it.document.close() }
        _uiState.value = UiState()
    }

    private fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private fun showFileChooser() {
        _uiState.value = _uiState.value.copy(showFileChooser = true)
    }

    private fun hideFileChooser() {
        _uiState.value = _uiState.value.copy(showFileChooser = false)
    }

    private fun showFolderChooser() {
        _uiState.value = _uiState.value.copy(showFolderChooser = true)
    }

    private fun hideFolderChooser() {
        _uiState.value = _uiState.value.copy(showFolderChooser = false)
    }

    private fun showSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = true)
    }

    private fun hideSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
        clearMessages()
    }

    private fun showErrorDialog() {
        _uiState.value = _uiState.value.copy(showErrorDialog = true)
    }

    private fun hideErrorDialog() {
        _uiState.value = _uiState.value.copy(showErrorDialog = false)
        clearMessages()
    }

    data class UiState(
        val isSplitting: Boolean = false,
        val isSaving: Boolean = false,
        val isMerging: Boolean = false,
        val showFileChooser: Boolean = false,
        val showFolderChooser: Boolean = false,
        val showSuccessDialog: Boolean = false,
        val showErrorDialog: Boolean = false,
        val selectedFile: File? = null,
        val errorMessage: String? = null,
        val successMessage: String? = null,
        val outputFileDestination: String? = null,
        val outputFileName: String = "",
        val splitProgress: Float = 0f,
        val saveProgress: Float = 0f,
        val mergeProgress: Float = 0f,
        val currentPageInfo: String? = null,
        val currentSaveInfo: String? = null,
        val currentMergeInfo: String? = null,
        val totalPages: Int = 0,
        val splitMode: SplitMode = SplitMode.SAVE_ALL,
        val extractedPages: List<ExtractedPage> = emptyList()
    )
}