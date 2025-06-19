@file:JvmName("SumPDF")
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import combiner.CombinerViewModel
import converter.ConverterViewModel
import moe.tlaster.precompose.ProvidePreComposeLocals
import org.jetbrains.compose.resources.painterResource
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import selectivesplitter.SelectiveSplitterViewModel
import splitter.SplitterViewModel
import sumpdf.resources.Res
import sumpdf.resources.sumpdf
import java.awt.Dimension
import java.io.File

fun main() = application {
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog")
    System.setProperty("jodconverter.document.format.registry", "simple")
    System.setProperty("com.google.gson.internal.UnsafeAllocator.disabled", "false")

    cleanupTempDirectories()

    Runtime.getRuntime().addShutdownHook(Thread {
        cleanupTempDirectories()
    })

    startKoin {
        modules(appModule)
    }

    val combinerViewModel = getKoin().get<CombinerViewModel>()
    val converterViewModel = getKoin().get<ConverterViewModel>()
    val splitterViewModel = getKoin().get<SplitterViewModel>()
    val selectiveSplitterViewModel = getKoin().get<SelectiveSplitterViewModel>()

    Window(
        onCloseRequest = {
            cleanupTempDirectories()
            exitApplication()
        },
        state = rememberWindowState(size = DpSize(1400.dp, 900.dp)),
        title = "SumPDF",
        alwaysOnTop = true,
        icon = painterResource(Res.drawable.sumpdf)
    ) {
        window.minimumSize = Dimension(1000, 700)

        ProvidePreComposeLocals {
            App(
                combinerViewModel = combinerViewModel,
                converterViewModel = converterViewModel,
                splitterViewModel = splitterViewModel,
                selectiveSplitterViewModel = selectiveSplitterViewModel
            )
        }

    }
}

private fun cleanupTempDirectories() {
    try {
        val tempDir = File(System.getProperty("java.io.tmpdir"))
        val sumpdfTempDirs = tempDir.listFiles { file ->
            file.isDirectory && file.name.startsWith("sumpdf_temp_")
        }

        sumpdfTempDirs?.forEach { dir ->
            try {
                dir.deleteRecursively()
                println("Cleaned up temp directory: ${dir.name}")
            } catch (e: Exception) {
                println("Failed to delete temp directory ${dir.name}: ${e.message}")
            }
        }
    } catch (e: Exception) {
        println("Error during temp directory cleanup: ${e.message}")
    }
}