sealed class PdfCombinerIntent {
    object AddPdfs : PdfCombinerIntent()
    data class RemovePdf(val pdfFile: PdfFile) : PdfCombinerIntent()
    object ClearAll : PdfCombinerIntent()
    data class SetOutputFileName(val name: String) : PdfCombinerIntent()
    object CombinePdfs : PdfCombinerIntent()
    object ClearMessages : PdfCombinerIntent()
}