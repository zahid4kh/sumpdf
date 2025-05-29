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
import org.jodconverter.core.office.OfficeException
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import java.io.File
import java.io.FileOutputStream
import javax.imageio.ImageIO

interface Converter {
    suspend fun convert(task: ConversionTask): ConversionResult
}

class PDFConverter : Converter {

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
                "odt" -> convertOdtToPdf(inputFile, outputFile)
                else -> throw IllegalArgumentException("Unsupported file format: ${inputFile.extension}")
            }

            ConversionResult(task.copy(outputFilePath = outputFile.absolutePath, status = ConversionStatus.COMPLETED), true)
        } catch (e: Exception) {
            e.printStackTrace()
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
            val page = PDPage(PDRectangle.A4)
            document.addPage(page)

            PDPageContentStream(document, page).use { contentStream ->
                val text = inputFile.readText()
                val lines = text.split("\n")

                contentStream.beginText()
                contentStream.setFont(PDType1Font(Standard14Fonts.FontName.HELVETICA), 12f)
                contentStream.newLineAtOffset(50f, 700f)
                contentStream.setLeading(14.5f)

                for (line in lines) {
                    contentStream.showText(line)
                    contentStream.newLine()
                }

                contentStream.endText()
            }

            document.save(outputFile)
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

    private fun convertOdtToPdf(inputFile: File, outputFile: File) {
        val officeManager = LocalOfficeManager.builder()
            .install()
            .build()

        try {
            officeManager.start()

            val converter = LocalConverter.builder()
                .officeManager(officeManager)
                .build()

            converter.convert(inputFile)
                .to(outputFile)
                .execute()
        } catch (e: OfficeException) {
            throw RuntimeException("Failed to convert ODT file: ${e.message}", e)
        } finally {
            officeManager.stop()
        }
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