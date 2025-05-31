package converter

import model.ConversionResult
import org.apache.batik.transcoder.TranscoderInput
import org.apache.batik.transcoder.TranscoderOutput
import org.apache.batik.transcoder.image.PNGTranscoder
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.jodconverter.core.document.DocumentFamily
import org.jodconverter.core.document.DocumentFormat
import org.jodconverter.core.document.DocumentFormatRegistry
import org.jodconverter.core.office.OfficeException
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO

interface Converter {
    suspend fun convert(task: ConversionTask): ConversionResult
}

class PDFConverter : Converter {

    companion object {
        private val COURIER_FONT by lazy {
            suppressAllFontWarnings()
            PDType1Font(Standard14Fonts.FontName.COURIER)
        }
    }

    override suspend fun convert(task: ConversionTask): ConversionResult {
        val inputFile = File(task.inputFilePath)
        if (!inputFile.exists()) {
            return ConversionResult(task, false, "Input file does not exist")
        }

        val outputPath = task.outputFilePath ?: determineOutputPath(inputFile.absolutePath)
        val outputFile = File(outputPath)

        outputFile.parentFile?.mkdirs()

        return try {
            when (inputFile.extension.lowercase()) {
                "txt" -> convertTextToPdf(inputFile, outputFile)
                "png", "jpg", "jpeg" -> convertImageToPdf(inputFile, outputFile)
                "svg" -> convertSvgToPdf(inputFile, outputFile)
                "odt" -> convertOfficeToPdf(inputFile, outputFile)
                "doc", "docx" -> convertOfficeToPdf(inputFile, outputFile)
                else -> throw IllegalArgumentException("Unsupported file format: ${inputFile.extension}")
            }

            if (outputFile.exists() && outputFile.length() > 0) {
                ConversionResult(task.copy(outputFilePath = outputFile.absolutePath, status = ConversionStatus.COMPLETED), true)
            } else {
                ConversionResult(task.copy(status = ConversionStatus.FAILED, error = "Output file was not created properly"), false, "Output file was not created properly")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            if (outputFile.exists()) {
                outputFile.delete()
            }
            ConversionResult(task.copy(status = ConversionStatus.FAILED, error = e.message), false, e.message)
        }
    }

    private fun determineOutputPath(inputPath: String): String {
        val inputFile = File(inputPath)
        val parentDir = inputFile.parentFile
        val baseName = inputFile.nameWithoutExtension

        return File(parentDir, "$baseName.pdf").absolutePath
    }

    private fun convertTextToPdf(inputFile: File, outputFile: File) {
        PDDocument().use { document ->
            val text = inputFile.readText(Charsets.UTF_8)
            val cleanedText = cleanTextForPdf(text)
            val lines = cleanedText.split("\n")

            val font = COURIER_FONT
            val fontSize = 10f
            val leading = 12f
            val margin = 50f
            val pageWidth = PDRectangle.A4.width
            val pageHeight = PDRectangle.A4.height
            val maxWidth = pageWidth - (2 * margin)

            var currentPage: PDPage? = null
            var contentStream: PDPageContentStream? = null
            var yPosition = pageHeight - margin

            try {
                for (line in lines) {
                    if (currentPage == null || yPosition < margin + leading) {
                        contentStream?.let {
                            try {
                                it.endText()
                                it.close()
                            } catch (e: Exception) {}
                        }

                        currentPage = PDPage(PDRectangle.A4)
                        document.addPage(currentPage)

                        contentStream = PDPageContentStream(document, currentPage)
                        contentStream.beginText()
                        contentStream.setFont(font, fontSize)
                        contentStream.setLeading(leading)

                        yPosition = pageHeight - margin
                        contentStream.newLineAtOffset(margin, yPosition)
                    }

                    val wrappedLines = wrapLinePreserveSpacing(line, font, fontSize, maxWidth)

                    for (wrappedLine in wrappedLines) {
                        if (yPosition < margin + leading) {
                            contentStream?.let {
                                try {
                                    it.endText()
                                    it.close()
                                } catch (e: Exception) {}
                            }

                            currentPage = PDPage(PDRectangle.A4)
                            document.addPage(currentPage)

                            contentStream = PDPageContentStream(document, currentPage)
                            contentStream.beginText()
                            contentStream.setFont(font, fontSize)
                            contentStream.setLeading(leading)

                            yPosition = pageHeight - margin
                            contentStream.newLineAtOffset(margin, yPosition)
                        }

                        try {
                            contentStream?.showText(wrappedLine)
                            contentStream?.newLine()
                            yPosition -= leading
                        } catch (e: Exception) {
                            println("Warning: Could not render line: $wrappedLine - ${e.message}")
                            contentStream?.newLine()
                            yPosition -= leading
                        }
                    }
                }
            } finally {
                contentStream?.let {
                    try {
                        it.endText()
                        it.close()
                    } catch (e: Exception) {}
                }
            }
            document.save(outputFile)
        }
    }

    private fun cleanTextForPdf(text: String): String {
        return text
            // tabs to 4 spaces (preserving indentation)
            .replace("\t", "    ")
            .replace("\u2011", "-") // non-breaking hyphen
            .replace("\u2010", "-") // hyphen
            .replace("\u2012", "-") // figure dash
            .replace("\u2013", "-") // en dash
            .replace("\u2014", "-") // em dash
            .replace("\u2015", "-") // horizontal bar
            .replace("\u201C", "\"") // left double quotation mark
            .replace("\u201D", "\"") // right double quotation mark
            .replace("\u2018", "'") // left single quotation mark
            .replace("\u2019", "'") // right single quotation mark
            .replace("\u2022", "*") // bullet
            .replace("\u2026", "...") // horizontal ellipsis
            .replace("\u00A0", " ") // non-breaking space
            //  keep line feeds
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .filter { char ->
                char == '\n' || char == ' ' || (char.code >= 32 && char.code <= 126)
            }
    }


    private fun wrapLinePreserveSpacing(line: String, font: PDType1Font, fontSize: Float, maxWidth: Float): List<String> {
        if (line.isEmpty()) return listOf("")

        try {
            val avgCharWidth = font.getStringWidth("M") / 1000 * fontSize
            val maxCharsPerLine = (maxWidth / avgCharWidth).toInt()

            if (line.length <= maxCharsPerLine) {
                return listOf(line)
            }

            val wrappedLines = mutableListOf<String>()
            var remainingLine = line

            while (remainingLine.length > maxCharsPerLine) {
                var splitPoint = maxCharsPerLine

                for (i in (maxCharsPerLine - 10).coerceAtLeast(0) until maxCharsPerLine) {
                    if (i < remainingLine.length) {
                        val char = remainingLine[i]
                        if (char == ' ' || char == '\t' || char in ".,;:!?-") {
                            splitPoint = i + 1
                            break
                        }
                    }
                }

                val linePortion = remainingLine.substring(0, splitPoint).trimEnd()
                wrappedLines.add(linePortion)

                remainingLine = remainingLine.substring(splitPoint).trimStart()
            }

            if (remainingLine.isNotEmpty()) {
                wrappedLines.add(remainingLine)
            }

            return wrappedLines.ifEmpty { listOf("") }
        } catch (e: Exception) {
            return listOf(line)
        }
    }

    private fun convertImageToPdf(inputFile: File, outputFile: File) {
        PDDocument().use { document ->
            val image = ImageIO.read(inputFile)
            val pdImage = PDImageXObject.createFromFile(inputFile.absolutePath, document)

            val imgWidth = image.width
            val imgHeight = image.height
            val pageWidth = PDRectangle.A4.width
            val pageHeight = pageWidth * imgHeight / imgWidth

            val page = PDPage(PDRectangle(pageWidth, pageHeight))
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                contentStream.drawImage(pdImage, 0f, 0f, pageWidth, pageHeight)
            }

            document.save(outputFile)
        }
    }

    private fun convertSvgToPdf(inputFile: File, outputFile: File) {
        val tempPng = File.createTempFile("svg_conv", ".png")
        tempPng.deleteOnExit()

        try {
            val input = TranscoderInput(inputFile.toURI().toString())
            val output = TranscoderOutput(FileOutputStream(tempPng))
            val transcoder = PNGTranscoder()
            transcoder.transcode(input, output)

            convertImageToPdf(tempPng, outputFile)
        } finally {
            tempPng.delete()
        }
    }

    private fun convertOfficeToPdf(inputFile: File, outputFile: File) {
        val officeManager = LocalOfficeManager.builder()
            .install()
            .processTimeout(30000L)
            .processRetryInterval(1000L)
            .maxTasksPerProcess(5)
            .portNumbers(2002, 2003, 2004)
            .build()

        try {
            officeManager.start()
            Thread.sleep(1000)

            val converter = LocalConverter.builder()
                .officeManager(officeManager)
                .formatRegistry(createCustomFormatRegistry())
                .build()

            converter.convert(inputFile)
                .to(outputFile)
                .execute()

        } catch (e: OfficeException) {
            val fileType = when (inputFile.extension.lowercase()) {
                "doc" -> "Word document (DOC)"
                "docx" -> "Word document (DOCX)"
                "odt" -> "OpenDocument Text (ODT)"
                else -> "${inputFile.extension.uppercase()} document"
            }
            throw RuntimeException("Failed to convert $fileType: ${e.message}", e)
        } finally {
            try {
                officeManager.stop()
                Thread.sleep(500)
            } catch (e: Exception) {}
        }
    }

    private fun createCustomFormatRegistry(): DocumentFormatRegistry {
        val formats = mutableMapOf<String, DocumentFormat>()

        formats["doc"] = createDocFormat()
        formats["docx"] = createDocxFormat()

        formats["odt"] = createOdtFormat()

        formats["pdf"] = createPdfFormat()

        return object : DocumentFormatRegistry {
            override fun getFormatByExtension(extension: String): DocumentFormat? {
                return formats[extension.lowercase()]
            }

            override fun getFormatByMediaType(mediaType: String): DocumentFormat? {
                return formats.values.find { it.mediaType == mediaType }
            }

            override fun getOutputFormats(family: DocumentFamily): MutableSet<DocumentFormat> {
                return when (family) {
                    DocumentFamily.TEXT -> {
                        mutableSetOf(formats["pdf"]!!)
                    }

                    else -> mutableSetOf()
                }
            }
        }
    }

    private fun createDocFormat(): DocumentFormat {
        return DocumentFormat.builder()
            .name("Microsoft Word 97-2003")
            .extension("doc")
            .mediaType("application/msword")
            .inputFamily(DocumentFamily.TEXT)
            .build()
    }

    private fun createDocxFormat(): DocumentFormat {
        return DocumentFormat.builder()
            .name("Microsoft Word 2007-2019")
            .extension("docx")
            .mediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
            .inputFamily(DocumentFamily.TEXT)
            .build()
    }

    private fun createOdtFormat(): DocumentFormat {
        return DocumentFormat.builder()
            .name("OpenDocument Text")
            .extension("odt")
            .mediaType("application/vnd.oasis.opendocument.text")
            .inputFamily(DocumentFamily.TEXT)
            .build()
    }

    private fun createPdfFormat(): DocumentFormat {
        // store properties map for PDF export
        //val storePropertiesMap = mutableMapOf<DocumentFamily, MutableMap<String, Any>>()
        val textProperties = mutableMapOf<String, Any>()
        textProperties["FilterName"] = "writer_pdf_Export"
        //storePropertiesMap[DocumentFamily.TEXT] = textProperties

        return DocumentFormat.builder()
            .name("Portable Document Format")
            .extension("pdf")
            .mediaType("application/pdf")
            .storeProperty(DocumentFamily.TEXT, "FilterName", "writer_pdf_Export")
            .build()
    }

    @Deprecated(
        message = "Using convertOfficeToPdf instead",
        replaceWith = ReplaceWith("convertOfficeToPdf(inputFile, outputFile)")
    )
    private fun convertOdtToPdf(inputFile: File, outputFile: File) {
        convertOfficeToPdf(inputFile, outputFile)
    }
}

private fun suppressAllFontWarnings() {
    try {
        val fontLoggers = listOf(
            "org.apache.fontbox",
            "org.apache.pdfbox.pdmodel.font",
            "org.apache.fontbox.ttf",
            "org.apache.fontbox.ttf.gsub",
            "org.apache.fontbox.ttf.GlyphSubstitutionTable",
            "org.apache.fontbox.ttf.gsub.GlyphSubstitutionDataExtractor",
            "org.apache.pdfbox.pdmodel.font.FileSystemFontProvider",
            "org.apache.pdfbox.pdmodel.font.FontMapperImpl",
            "org.apache.fontbox.ttf.CmapSubtable"
        )

        fontLoggers.forEach { loggerName ->
            Logger.getLogger(loggerName).level = Level.OFF
        }

        Logger.getLogger("org.apache.fontbox").level = Level.OFF
        Logger.getLogger("org.apache.pdfbox").level = Level.SEVERE

    } catch (e: Exception) { }
}