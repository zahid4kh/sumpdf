package splitter

import combiner.PdfFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.apache.pdfbox.Loader
import org.apache.pdfbox.cos.COSDocument
import org.apache.pdfbox.multipdf.Splitter
import org.apache.pdfbox.pdmodel.PDDocument
import java.io.File
import java.io.IOException

class SplitterViewModel {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)


    fun splitWholePdf(file: File){
        val document: PDDocument = Loader.loadPDF(file)
        val splitter = Splitter()

        val pages: List<PDDocument> = splitter.split(document)
        val name = setOutputFileName(file.nameWithoutExtension+"_")
        val outputPath = selectOutputPath(_uiState.value.outputFileDestination)

        try{
            var num = 1
            for(doc in pages){
                doc.save("$outputPath$name$num.pdf")
                num++
                doc.close()
            }
        }catch(e: IOException){
            e.printStackTrace()
        }

    }

    fun addPdfFile(file: File){
        if (file.extension.lowercase() == "pdf") {
            _uiState.value = _uiState.value.copy(
                selectedFile = file,
                errorMessage = null
            )
            _uiState.value = _uiState.value.copy(
                selectedFile = File(file.path),
            )
        } else {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a valid PDF file."
            )
            _uiState.value = _uiState.value.copy(showErrorDialog = true)
        }
        hideFileChooser()
    }

    fun selectOutputPath(path: String?): String?{
        _uiState.value = _uiState.value.copy(
            outputFileDestination = path
        )
        return path
    }

    private fun clear() {
        _uiState.value = _uiState.value.copy(selectedFile = null)
    }

    private fun setOutputFileName(name: String): String {
        _uiState.value = _uiState.value.copy(outputFileName = name)
        return name
    }

    private fun showFileChooser() {
        _uiState.value = _uiState.value.copy(showFileChooser = true)
    }

    private fun hideFileChooser() {
        _uiState.value = _uiState.value.copy(showFileChooser = false)
    }

    private fun showFileSaver() {
        if (_uiState.value.selectedFile == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please add at least one PDF file.")
            _uiState.value = _uiState.value.copy(showErrorDialog = true)
            return
        }
        _uiState.value = _uiState.value.copy(showFileSaver = true)
    }

    private fun hideFileSaver() {
        _uiState.value = _uiState.value.copy(showFileSaver = false)
    }


    data class UiState(
        val isSplitting: Boolean = false,
        val showFileChooser: Boolean = false,
        val showFileSaver: Boolean = false,
        val showSuccessDialog: Boolean = false,
        val showErrorDialog: Boolean = false,
        val selectedFile: File? = null,
        val errorMessage: String? = null,
        val successMessage: String? = null,
        val isLoading: Boolean = false,
        val num: Int = 0,
        val outputFileDestination: String? = "/home/zahid/Documents/test/",
        val outputFileName: String? = selectedFile?.name + "_"
    )
}