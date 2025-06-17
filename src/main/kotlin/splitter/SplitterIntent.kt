package splitter

import java.io.File

sealed class SplitterIntent {
    object ShowFileChooser : SplitterIntent()
    object HideFileChooser : SplitterIntent()
    object ShowFolderChooser : SplitterIntent()
    object HideFolderChooser : SplitterIntent()
    data class AddPdfFile(val file: File) : SplitterIntent()
    data class SelectOutputPath(val path: String) : SplitterIntent()
    data class SetOutputFileName(val name: String) : SplitterIntent()
    object SplitPdf : SplitterIntent()
    object ClearAll : SplitterIntent()
    object ClearMessages : SplitterIntent()
    object ShowSuccessDialog : SplitterIntent()
    object HideSuccessDialog : SplitterIntent()
    object ShowErrorDialog : SplitterIntent()
    object HideErrorDialog : SplitterIntent()
    data class SelectSplitMode(val mode: SplitMode) : SplitterIntent()
    data class DeleteExtractedPage(val page: ExtractedPage) : SplitterIntent()
    data class MovePageLeft(val page: ExtractedPage) : SplitterIntent()
    data class MovePageRight(val page: ExtractedPage) : SplitterIntent()
    object SaveExtractedPages : SplitterIntent()
    object MergeAndSavePages : SplitterIntent()
}

enum class SplitMode {
    SAVE_ALL,
    DELETE_PAGES,
    MERGE_PAGES
}

data class ExtractedPage(
    val id: String,
    val pageNumber: Int,
    val fileName: String,
    val document: org.apache.pdfbox.pdmodel.PDDocument,
    val size: Long = 0L
)