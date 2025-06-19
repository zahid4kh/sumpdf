package selectivesplitter

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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdfwriter.compress.CompressParameters
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File
import java.io.IOException
import java.util.*

class SelectiveSplitterViewModel(
    private val database: Database
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)

    fun handleIntent(intent: SelectiveSplitterIntent) {
        when (intent) {
            is SelectiveSplitterIntent.ShowFileChooser -> showFileChooser()
            is SelectiveSplitterIntent.HideFileChooser -> hideFileChooser()
            is SelectiveSplitterIntent.ShowFolderChooser -> showFolderChooser()
            is SelectiveSplitterIntent.HideFolderChooser -> hideFolderChooser()
            is SelectiveSplitterIntent.AddPdfFile -> addPdfFile(intent.file)
            is SelectiveSplitterIntent.SelectOutputPath -> selectOutputPath(intent.path)
            is SelectiveSplitterIntent.SetOutputFileName -> setOutputFileName(intent.name)
            is SelectiveSplitterIntent.SetStartPage -> setStartPage(intent.page)
            is SelectiveSplitterIntent.SetEndPage -> setEndPage(intent.page)
            is SelectiveSplitterIntent.ExtractPages -> extractPages()
            is SelectiveSplitterIntent.ClearAll -> clearAll()
            is SelectiveSplitterIntent.ClearMessages -> clearMessages()
            is SelectiveSplitterIntent.ShowSuccessDialog -> showSuccessDialog()
            is SelectiveSplitterIntent.HideSuccessDialog -> hideSuccessDialog()
            is SelectiveSplitterIntent.ShowErrorDialog -> showErrorDialog()
            is SelectiveSplitterIntent.HideErrorDialog -> hideErrorDialog()
            is SelectiveSplitterIntent.DeleteExtractedPage -> deleteExtractedPage(intent.page)
            is SelectiveSplitterIntent.MovePageLeft -> movePageLeft(intent.page)
            is SelectiveSplitterIntent.MovePageRight -> movePageRight(intent.page)
            is SelectiveSplitterIntent.SaveExtractedPages -> saveExtractedPages()
            is SelectiveSplitterIntent.MergeAndSavePages -> mergeAndSavePages()
            is SelectiveSplitterIntent.StartDeleteAnimation -> startDeleteAnimation(intent.page)
            is SelectiveSplitterIntent.StartMoveAnimation -> startMoveAnimation(intent.page, intent.direction)
        }
    }

    private fun extractPages() {
        val selectedFile = _uiState.value.selectedFile
        val startPage = _uiState.value.startPage.toIntOrNull()
        val endPage = _uiState.value.endPage.toIntOrNull()
        val outputPrefix = _uiState.value.outputFileName

        if (selectedFile == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a PDF file.",
                showErrorDialog = true
            )
            return
        }

        if (startPage == null || endPage == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter valid start and end page numbers.",
                showErrorDialog = true
            )
            return
        }

        if (startPage < 1 || endPage < 1 || startPage > _uiState.value.totalPages || endPage > _uiState.value.totalPages) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Page numbers must be between 1 and ${_uiState.value.totalPages}.",
                showErrorDialog = true
            )
            return
        }

        if (startPage > endPage) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Start page must be less than or equal to end page.",
                showErrorDialog = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            isExtracting = true,
            extractProgress = 0f,
            currentPageInfo = "Initializing...",
            errorMessage = null,
            extractedPages = emptyList(),
            animatingNewPageIds = emptySet()
        )

        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val document: PDDocument = Loader.loadPDF(selectedFile)
                    val totalPagesToExtract = endPage - startPage + 1

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            currentPageInfo = "Extracting pages $startPage-$endPage ($totalPagesToExtract pages)..."
                        )
                    }

                    delay(300)

                    val tempDir = File(System.getProperty("java.io.tmpdir"), "sumpdf_selective_${System.currentTimeMillis()}")
                    tempDir.mkdirs()
                    println("Temp directory: $tempDir")

                    for (pageNum in startPage..endPage) {
                        val extractor = org.apache.pdfbox.multipdf.PageExtractor(document, pageNum, pageNum)
                        val extractedDoc = extractor.extract()

                        val fileName = "${outputPrefix}page_$pageNum.pdf"
                        val tempFile = File(tempDir, fileName)

                        withContext(Dispatchers.Main) {
                            _uiState.value = _uiState.value.copy(
                                currentPageInfo = "Extracting page $pageNum of $endPage...",
                                extractProgress = ((pageNum - startPage + 1).toFloat() / totalPagesToExtract)
                            )
                        }

                        extractedDoc.save(tempFile.absolutePath)
                        val fileSize = tempFile.length()
                        extractedDoc.close()

                        val newPage = ExtractedPage(
                            id = UUID.randomUUID().toString(),
                            pageNumber = pageNum,
                            fileName = fileName,
                            tempFilePath = tempFile.absolutePath,
                            size = fileSize
                        )

                        withContext(Dispatchers.Main) {
                            val currentPages = _uiState.value.extractedPages.toMutableList()
                            currentPages.add(newPage)
                            val newAnimatingIds = _uiState.value.animatingNewPageIds.toMutableSet()
                            newAnimatingIds.add(newPage.id)

                            _uiState.value = _uiState.value.copy(
                                extractedPages = currentPages,
                                animatingNewPageIds = newAnimatingIds
                            )

                            scope.launch {
                                delay(600)
                                val updatedAnimatingIds = _uiState.value.animatingNewPageIds.toMutableSet()
                                updatedAnimatingIds.remove(newPage.id)
                                _uiState.value = _uiState.value.copy(
                                    animatingNewPageIds = updatedAnimatingIds
                                )
                            }
                        }

                        delay(150)
                    }

                    document.close()

                    withContext(Dispatchers.Main) {
                        _uiState.value = _uiState.value.copy(
                            isExtracting = false,
                            extractProgress = 1f,
                            currentPageInfo = null,
                            successMessage = "Successfully extracted $totalPagesToExtract pages! Review and manage pages below."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExtracting = false,
                    errorMessage = "Failed to extract pages: ${e.message}",
                    showErrorDialog = true,
                    currentPageInfo = null,
                    extractProgress = 0f
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

                    val mergedFileName = "${outputPrefix}merged_range.pdf"
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
            try {
                val document: PDDocument = Loader.loadPDF(file)
                val numOfPages = document.numberOfPages
                document.close()

                _uiState.value = _uiState.value.copy(
                    selectedFile = file,
                    totalPages = numOfPages,
                    outputFileName = "${file.nameWithoutExtension}_",
                    errorMessage = null,
                    extractedPages = emptyList(),
                    startPage = "1",
                    endPage = numOfPages.toString()
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load PDF: ${e.message}"
                )
                showErrorDialog()
            }
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

    private fun setStartPage(page: String) {
        _uiState.value = _uiState.value.copy(startPage = page)
    }

    private fun setEndPage(page: String) {
        _uiState.value = _uiState.value.copy(endPage = page)
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
        val isExtracting: Boolean = false,
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
        val startPage: String = "1",
        val endPage: String = "1",
        val extractProgress: Float = 0f,
        val saveProgress: Float = 0f,
        val mergeProgress: Float = 0f,
        val currentPageInfo: String? = null,
        val currentSaveInfo: String? = null,
        val currentMergeInfo: String? = null,
        val totalPages: Int = 0,
        val extractedPages: List<ExtractedPage> = emptyList(),
        val animatingDeletePageId: String? = null,
        val animatingMovePageId: String? = null,
        val moveDirection: String? = null,
        val animatingNewPageIds: Set<String> = emptySet()
    )
}