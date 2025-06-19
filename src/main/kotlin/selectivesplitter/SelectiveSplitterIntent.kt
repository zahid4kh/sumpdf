package selectivesplitter

import java.io.File

sealed class SelectiveSplitterIntent {
    object ShowFileChooser : SelectiveSplitterIntent()
    object HideFileChooser : SelectiveSplitterIntent()
    object ShowFolderChooser : SelectiveSplitterIntent()
    object HideFolderChooser : SelectiveSplitterIntent()
    data class AddPdfFile(val file: File) : SelectiveSplitterIntent()
    data class SelectOutputPath(val path: String) : SelectiveSplitterIntent()
    data class SetOutputFileName(val name: String) : SelectiveSplitterIntent()
    data class SetStartPage(val page: String) : SelectiveSplitterIntent()
    data class SetEndPage(val page: String) : SelectiveSplitterIntent()
    object ExtractPages : SelectiveSplitterIntent()
    object ClearAll : SelectiveSplitterIntent()
    object ClearMessages : SelectiveSplitterIntent()
    object ShowSuccessDialog : SelectiveSplitterIntent()
    object HideSuccessDialog : SelectiveSplitterIntent()
    object ShowErrorDialog : SelectiveSplitterIntent()
    object HideErrorDialog : SelectiveSplitterIntent()
    data class DeleteExtractedPage(val page: ExtractedPage) : SelectiveSplitterIntent()
    data class MovePageLeft(val page: ExtractedPage) : SelectiveSplitterIntent()
    data class MovePageRight(val page: ExtractedPage) : SelectiveSplitterIntent()
    object SaveExtractedPages : SelectiveSplitterIntent()
    object MergeAndSavePages : SelectiveSplitterIntent()
    data class StartDeleteAnimation(val page: ExtractedPage) : SelectiveSplitterIntent()
    data class StartMoveAnimation(val page: ExtractedPage, val direction: String) : SelectiveSplitterIntent()
}

data class ExtractedPage(
    val id: String,
    val pageNumber: Int,
    val fileName: String,
    val tempFilePath: String,
    val size: Long = 0L
)