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
import sumpdf.resources.Res
import sumpdf.resources.sumpdf
import java.awt.Dimension

fun main() = application {
    System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog")

    System.setProperty("jodconverter.document.format.registry", "simple")
    System.setProperty("com.google.gson.internal.UnsafeAllocator.disabled", "false")

    startKoin {
        modules(appModule)
    }

    val combinerViewModel = getKoin().get<CombinerViewModel>()
    val converterViewModel = getKoin().get<ConverterViewModel>()

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(1400.dp, 900.dp)),
        title = "SumPDF",
        alwaysOnTop = true,
        icon = painterResource(Res.drawable.sumpdf)
    ) {
        window.minimumSize = Dimension(1000, 700)

        ProvidePreComposeLocals {
            App(
                combinerViewModel = combinerViewModel,
                converterViewModel = converterViewModel
            )
        }

    }
}