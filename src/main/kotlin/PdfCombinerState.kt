import java.io.File

data class PdfCombinerState(
    val pdfFiles: List<PdfFile> = emptyList(),
    val outputFileName: String = "combined_document",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

data class PdfFile(
    val id: String,
    val name: String,
    val path: String
) {
    companion object {
        fun from(file: File) = PdfFile(
            id = System.nanoTime().toString(),
            name = file.nameWithoutExtension,
            path = file.absolutePath
        )
    }
}