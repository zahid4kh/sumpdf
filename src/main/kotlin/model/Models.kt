package model

import java.io.File

data class ConversionResult(
    val task: converter.ConversionTask,
    val success: Boolean,
    val error: String? = null
)

data class FileItem(
    val file: File,
    val isSelected: Boolean = true
)