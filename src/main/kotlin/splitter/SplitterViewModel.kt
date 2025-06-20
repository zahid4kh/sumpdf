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
            is SplitterIntent.StartDeleteAnimation -> startDeleteAnimation(intent.page)
            is SplitterIntent.StartMoveAnimation -> startMoveAnimation(intent.page, intent.direction)
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
                        val tempDir = File(System.getProperty("java.io.tmpdir"), "sumpdf_temp_${System.currentTimeMillis()}")
                        tempDir.mkdirs()
                        println("Temp directory: $tempDir")

                        val extractedPages = pages.mapIndexed { index, pageDoc ->
                            val pageNumber = index + 1
                            val fileName = "${outputPrefix}page_$pageNumber.pdf"

                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    currentPageInfo = "Extracting page $pageNumber of $totalPages...",
                                    splitProgress = (pageNumber.toFloat() / totalPages)
                                )
                            }

                            val tempFile = File(tempDir, fileName)
                            pageDoc.save(tempFile.absolutePath)
                            val fileSize = tempFile.length()
                            pageDoc.close()

                            delay(150)

                            ExtractedPage(
                                id = UUID.randomUUID().toString(),
                                pageNumber = pageNumber,
                                fileName = fileName,
                                tempFilePath = tempFile.absolutePath,
                                size = fileSize
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
                            val tempFile = File(page.tempFilePath)

                            withContext(Dispatchers.Main) {
                                _uiState.value = _uiState.value.copy(
                                    currentSaveInfo = "Saving ${page.fileName} (${index + 1}/${extractedPages.size})...",
                                    saveProgress = ((index + 1).toFloat() / extractedPages.size)
                                )
                            }

                            tempFile.copyTo(outputFile, overwrite = true)
                            successCount++

                            delay(300)
                        } catch (e: IOException) {
                            failureCount++
                            println("Failed to save ${page.fileName}: ${e.message}")
                        }
                    }

                    extractedPages.forEach { page ->
                        try {
                            File(page.tempFilePath).delete()
                            File(page.tempFilePath).parentFile?.let { parent ->
                                if (parent.listFiles()?.isEmpty() == true) {
                                    parent.delete()
                                }
                            }
                        } catch (e: Exception) {}
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

                        merger.addSource(File(page.tempFilePath))
                        delay(200)
                    }

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            currentMergeInfo = "Finalizing merged document...",
                            mergeProgress = 0.9f
                        )
                    }

                    merger.mergeDocuments(null, CompressParameters(500))

                    extractedPages.forEach { page ->
                        try {
                            File(page.tempFilePath).delete()
                            File(page.tempFilePath).parentFile?.let { parent ->
                                if (parent.listFiles()?.isEmpty() == true) {
                                    parent.delete()
                                }
                            }
                        } catch (e: Exception) {}
                    }

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
        val currentMode = _uiState.value.splitMode

        val shouldClearPages = when {
            mode == SplitMode.SAVE_ALL -> true
            currentMode == SplitMode.SAVE_ALL -> true
            else -> false
        }

        _uiState.value = _uiState.value.copy(
            splitMode = mode,
            extractedPages = if (shouldClearPages) emptyList() else _uiState.value.extractedPages
        )
    }

    private fun deleteExtractedPage(page: ExtractedPage) {
        val currentPages = _uiState.value.extractedPages.toMutableList()
        currentPages.remove(page)

        try {
            File(page.tempFilePath).delete()
        } catch (e: Exception) { }

        _uiState.value = _uiState.value.copy(extractedPages = currentPages)
    }

    private fun movePageLeft(page: ExtractedPage) {
        val currentPages = _uiState.value.extractedPages.toMutableList()
        val currentIndex = currentPages.indexOf(page)
        if (currentIndex > 0) {
            currentPages.removeAt(currentIndex)
            currentPages.add(currentIndex - 1, page)
            _uiState.value = _uiState.value.copy(extractedPages = currentPages, moveDirection = "left")
        }
    }

    private fun movePageRight(page: ExtractedPage) {
        val currentPages = _uiState.value.extractedPages.toMutableList()
        val currentIndex = currentPages.indexOf(page)
        if (currentIndex < currentPages.size - 1) {
            currentPages.removeAt(currentIndex)
            currentPages.add(currentIndex + 1, page)
            _uiState.value = _uiState.value.copy(extractedPages = currentPages, moveDirection = "right")
        }
    }

    private fun addPdfFile(file: File) {
        if (file.extension.lowercase() == "pdf") {
            val document: PDDocument = Loader.loadPDF(file)
            val numOfPages = document.numberOfPages
            document.close()
            _uiState.value = _uiState.value.copy(
                selectedFile = file,
                totalPages = numOfPages,
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
        _uiState.value.extractedPages.forEach { page ->
            try {
                File(page.tempFilePath).delete()
                File(page.tempFilePath).parentFile?.let { parent ->
                    if (parent.listFiles()?.isEmpty() == true) {
                        parent.delete()
                    }
                }
            } catch (e: Exception) {}
        }

        _uiState.value = UiState()
    }

    private fun startDeleteAnimation(page: ExtractedPage) {
        _uiState.value = _uiState.value.copy(animatingDeletePageId = page.id)

        scope.launch {
            delay(300)
            deleteExtractedPage(page)
            _uiState.value = _uiState.value.copy(animatingDeletePageId = null)
        }
    }

    private fun startMoveAnimation(page: ExtractedPage, direction: String) {
        _uiState.value = _uiState.value.copy(
            animatingMovePageId = page.id,
            moveDirection = direction
        )

        scope.launch {
            delay(400)
            if (direction == "left") {
                movePageLeft(page)
            } else {
                movePageRight(page)
            }
            _uiState.value = _uiState.value.copy(
                animatingMovePageId = null,
                moveDirection = null
            )
        }
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
        _uiState.value = UiState()
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
        val extractedPages: List<ExtractedPage> = emptyList(),
        val animatingDeletePageId: String? = null,
        val animatingMovePageId: String? = null,
        val moveDirection: String? = null
    )
}