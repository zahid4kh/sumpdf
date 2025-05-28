package converter

import kotlinx.serialization.Serializable

@Serializable
data class ConversionTask(
    val id: String,
    val inputFilePath: String,
    val outputFilePath: String? = null,
    val status: ConversionStatus = ConversionStatus.PENDING,
    val error: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ConversionStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}

@Serializable
data class ConverterSettings(
    val defaultOutputPath: String? = null,
    val recentOutputPaths: List<String> = emptyList(),
    val lastUsedDirectory: String? = null
)