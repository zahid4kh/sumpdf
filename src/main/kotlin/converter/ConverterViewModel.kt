package converter

import Database
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import model.ConversionResult
import model.FileItem
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.filter
import kotlin.collections.map
import kotlin.collections.toMutableList

class ConverterViewModel(
    private val database: Database,
    private val converter: Converter
) {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val homeDir = System.getProperty("user.home")
    val downloadsDir = "$homeDir/Downloads"

    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        scope.launch {
            val settings = database.getConverterSettings()
            _uiState.value = _uiState.value.copy(
                defaultOutputPath = settings.defaultOutputPath,
                recentOutputPaths = settings.recentOutputPaths,
                lastUsedDirectory = settings.lastUsedDirectory,
                selectedOutputPath = downloadsDir
            )

            val previousTasks = database.getTasks()
            if (previousTasks.isNotEmpty()) {
                _uiState.value = _uiState.value.copy(
                    previousTasks = previousTasks
                )
            }
        }
    }

    fun addFiles(files: List<File>) {
        val currentFiles = _uiState.value.files.toMutableList()
        val newFiles = files.map { FileItem(it) }
        log("(addFiles) Added ${files.size} files for conversion")

        currentFiles.addAll(newFiles)
        _uiState.value = _uiState.value.copy(files = currentFiles)
    }

    fun removeFile(file: FileItem) {
        log("(removeFile) Removing file: ${file.file.name}")
        val updatedFiles = _uiState.value.files.toMutableList()
        updatedFiles.remove(file)
        _uiState.value = _uiState.value.copy(files = updatedFiles)
    }

    fun toggleFileSelection(file: FileItem) {
        val updatedFiles = _uiState.value.files.toMutableList()
        val index = updatedFiles.indexOf(file)
        if (index >= 0) {
            val newSelectionState = !file.isSelected
            log("(toggleFileSelection) ${if (newSelectionState) "Selected" else "Deselected"} file: ${file.file.name}")
            updatedFiles[index] = file.copy(isSelected = newSelectionState)
            _uiState.value = _uiState.value.copy(files = updatedFiles)
        }
    }

    fun selectOutputPath(path: String?) {
        if (path != null) {
            log("(selectOutputPath) Output path set to: $path")
            val recentPaths = _uiState.value.recentOutputPaths.toMutableList()

            if (path !in recentPaths) {
                recentPaths.add(0, path)
                if (recentPaths.size > 5) {
                    recentPaths.removeAt(recentPaths.size - 1)
                }
            } else {
                recentPaths.remove(path)
                recentPaths.add(0, path)
            }

            _uiState.value = _uiState.value.copy(
                selectedOutputPath = path,
                recentOutputPaths = recentPaths
            )

            scope.launch {
                val settings = database.getConverterSettings()
                database.saveConverterSettings(settings.copy(
                    defaultOutputPath = path,
                    recentOutputPaths = recentPaths
                ))
            }
        }
    }

    fun selectDownloadsFolder(){
        log("(selectDownloadsFolder) Selected Downloads folder as output path")
        _uiState.value = _uiState.value.copy(
            isDownloadsPathSelected = true,
            isCustomPathSelected = false,
            selectedOutputPath = downloadsDir
        )
    }

    fun selectCustomPath(){
        log("(selectCustomPath) Selected custom path option")
        _uiState.value = _uiState.value.copy(
            isCustomPathSelected = true,
            isDownloadsPathSelected = false
        )
    }

    fun setLastUsedDirectory(path: String) {
        log("(setLastUsedDirectory) Last used directory set to: $path")
        _uiState.value = _uiState.value.copy(lastUsedDirectory = path)

        scope.launch {
            val settings = database.getConverterSettings()
            database.saveConverterSettings(settings.copy(lastUsedDirectory = path))
        }
    }

    fun showFileChooser() {
        log("(showFileChooser) Opening file chooser dialog")
        _uiState.value = _uiState.value.copy(showFileChooser = true)
    }

    fun hideFileChooser() {
        log("(hideFileChooser) Closing file chooser dialog")
        _uiState.value = _uiState.value.copy(showFileChooser = false)
    }

    fun showFolderChooser() {
        log("(showFolderChooser) Opening folder chooser dialog")
        _uiState.value = _uiState.value.copy(showFolderChooser = true)
    }

    fun hideFolderChooser() {
        log("(hideFolderChooser) Closing folder chooser dialog")
        _uiState.value = _uiState.value.copy(showFolderChooser = false)
    }

    fun convertFiles() {
        val selectedFiles = _uiState.value.files.filter { it.isSelected }
        if (selectedFiles.isEmpty()) return
        log("(convertFiles) Starting conversion of ${selectedFiles.size} files")

        _uiState.value = _uiState.value.copy(isConverting = true)

        scope.launch {
            val tasks = selectedFiles.map { fileItem ->
                val outputPath = determineOutputPath(fileItem.file)

                ConversionTask(
                    id = UUID.randomUUID().toString(),
                    inputFilePath = fileItem.file.absolutePath,
                    outputFilePath = outputPath,
                    status = ConversionStatus.PENDING
                )
            }

            _uiState.value = _uiState.value.copy(
                conversionTasks = tasks,
                conversionProgress = 0f,
                currentlyConverting = null
            )

            database.saveTasks(tasks)

            val results = mutableListOf<ConversionResult>()

            for ((index, task) in tasks.withIndex()) {
                val fileName = File(task.inputFilePath).name
                log("(convertFiles) Converting file ${index + 1}/${tasks.size}: $fileName")
                _uiState.value = _uiState.value.copy(
                    currentlyConverting = "Converting: $fileName (${index + 1}/${tasks.size})"
                )

                updateTaskStatus(task.id, ConversionStatus.IN_PROGRESS)

                val result = withContext(Dispatchers.IO) {
                    converter.convert(task)
                }
                if (result.success) {
                    log("(convertFiles) Successfully converted: ${File(task.inputFilePath).name} -> ${File(result.task.outputFilePath!!).name}")
                } else {
                    log("(convertFiles) Failed to convert: ${File(task.inputFilePath).name} - ${result.error}")
                }
                results.add(result)

                val progress = (index + 1).toFloat() / tasks.size
                _uiState.value = _uiState.value.copy(conversionProgress = progress)

                updateTaskStatus(
                    task.id,
                    if (result.success) ConversionStatus.COMPLETED else ConversionStatus.FAILED,
                    result.error
                )
            }

            _uiState.value = _uiState.value.copy(
                isConverting = false,
                conversionResults = results,
                showResultDialog = true,
                currentlyConverting = null
            )

            log("(convertFiles) Conversion completed: ${results.count { it.success }} successful, ${results.count { !it.success }} failed")
            val allTasks = _uiState.value.previousTasks + _uiState.value.conversionTasks
            database.saveTasks(allTasks)
        }
    }

    private fun updateTaskStatus(taskId: String, status: ConversionStatus, error: String? = null) {
        log("(updateTaskStatus) Task $taskId status updated to: $status${if (error != null) " with error: $error" else ""}")
        val updatedTasks = _uiState.value.conversionTasks.map { task ->
            if (task.id == taskId) {
                task.copy(status = status, error = error)
            } else {
                task
            }
        }
        _uiState.value = _uiState.value.copy(conversionTasks = updatedTasks)
    }

    private fun determineOutputPath(file: File): String {
        val selectedPath = _uiState.value.selectedOutputPath
        val outputPath = if (selectedPath != null) {
            File(selectedPath, "${file.nameWithoutExtension}.pdf").absolutePath
        } else {
            File(file.parentFile, "${file.nameWithoutExtension}.pdf").absolutePath
        }
        log("(determineOutputPath) Output path for ${file.name}: $outputPath")
        return outputPath
    }

    fun dismissResultDialog() {
        log("(dismissResultDialog) Dismissing conversion result dialog")
        val completedTasks = _uiState.value.conversionTasks
        _uiState.value = _uiState.value.copy(
            showResultDialog = false,
            conversionResults = emptyList(),
            conversionTasks = emptyList(),
            previousTasks = _uiState.value.previousTasks + completedTasks
        )
    }

    fun clearFiles() {
        log("(clearFiles) Clearing all files (${_uiState.value.files.size} files)")
        _uiState.value = _uiState.value.copy(files = emptyList())
    }

    private fun log(message: String) {
        val timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
        )
        println("[$timestamp] [${this::class.simpleName}] $message")
    }

    data class UiState(
        val darkMode: Boolean = false,
        val files: List<FileItem> = emptyList(),
        val isConverting: Boolean = false,
        val conversionProgress: Float = 0f,
        val currentlyConverting: String? = null,
        val conversionTasks: List<ConversionTask> = emptyList(),
        val previousTasks: List<ConversionTask> = emptyList(),
        val conversionResults: List<ConversionResult> = emptyList(),
        val showResultDialog: Boolean = false,
        val selectedOutputPath: String? = null,
        val recentOutputPaths: List<String> = emptyList(),
        val lastUsedDirectory: String? = null,
        val defaultOutputPath: String? = null,
        val showFileChooser: Boolean = false,
        val showFolderChooser: Boolean = false,
        val isDownloadsPathSelected: Boolean = true,
        val isCustomPathSelected: Boolean = false
    )
}