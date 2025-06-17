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
}