package combiner

import java.io.File

sealed class PdfCombinerIntent {
    object ShowFileChooser : PdfCombinerIntent()
    object HideFileChooser : PdfCombinerIntent()
    data class AddPdf(val file: File) : PdfCombinerIntent()
    data class RemovePdf(val pdfFile: PdfFile) : PdfCombinerIntent()
    data class ReorderPdfs(val fromIndex: Int, val toIndex: Int) : PdfCombinerIntent()
    object ClearAll : PdfCombinerIntent()
    data class SetOutputFileName(val name: String) : PdfCombinerIntent()
    object ShowFileSaver : PdfCombinerIntent()
    object HideFileSaver : PdfCombinerIntent()
    object CombinePdfs : PdfCombinerIntent()
    object ClearMessages : PdfCombinerIntent()
    object ShowSuccessDialog : PdfCombinerIntent()
    object HideSuccessDialog : PdfCombinerIntent()
    object ShowErrorDialog : PdfCombinerIntent()
    object HideErrorDialog : PdfCombinerIntent()
    object CheckForUpdates : PdfCombinerIntent()
}