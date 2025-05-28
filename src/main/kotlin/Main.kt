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
import theme.AppTheme
import java.awt.Dimension
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.getKoin
import sumpdf.resources.Res
import sumpdf.resources.sumpdf

fun main() = application {
    startKoin {
        modules(appModule)
    }

    val combinerViewModel = getKoin().get<CombinerViewModel>()
    val converterViewModel = getKoin().get<ConverterViewModel>()

    Window(
        onCloseRequest = ::exitApplication,
        state = rememberWindowState(size = DpSize(900.dp, 700.dp)),
        title = "SumPDF",
        alwaysOnTop = true,
        icon = painterResource(Res.drawable.sumpdf)
    ) {
        window.minimumSize = Dimension(900, 700)

        ProvidePreComposeLocals {
            App(
                combinerViewModel = combinerViewModel,
                converterViewModel = converterViewModel
            )
        }

    }
}