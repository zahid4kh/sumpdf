import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import combiner.CombinerViewModel
import converter.ConverterViewModel
import moe.tlaster.precompose.PreComposeApp
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import theme.AppTheme
import combiner.CombinerScreen
import converter.ConverterScreen
import selectivesplitter.SelectiveSplitterScreen
import selectivesplitter.SelectiveSplitterViewModel
import splitter.SplitterScreen
import splitter.SplitterViewModel

@Composable
fun App(
    combinerViewModel: CombinerViewModel,
    converterViewModel: ConverterViewModel,
    splitterViewModel: SplitterViewModel,
    selectiveSplitterViewModel: SelectiveSplitterViewModel
) {
    val combinerUiState by combinerViewModel.uiState.collectAsState()

    // Using combiner's dark mode as the single source of truth
    // users won't even know
    val darkMode = combinerUiState.darkMode

    PreComposeApp {
        AppTheme(darkTheme = darkMode) {
            val navigator = rememberNavigator()

            NavHost(
                navigator = navigator,
                navTransition = NavTransition(),
                initialRoute = "/main"
            ) {
                scene("/main") {
                    MainNavigationScreen(
                        darkMode = darkMode,
                        onToggleDarkMode = combinerViewModel::toggleDarkMode,
                        onNavigateToCombiner = { navigator.navigate("/combiner") },
                        onNavigateToConverter = { navigator.navigate("/converter") },
                        onNavigateToSplitter = {navigator.navigate("/splitter")},
                        onNavigateToSelectiveSplitter = {navigator.navigate("/selectivesplitter")},
                        viewModel = combinerViewModel
                    )
                }

                scene("/combiner",
                    navTransition = NavTransition(
                        createTransition = slideInHorizontally() + scaleIn(initialScale = 0.4f),
                        destroyTransition = scaleOut()
                    )
                ) {
                    CombinerScreen(
                        viewModel = combinerViewModel,
                        onBack = { navigator.goBack() }
                    )
                }

                scene("/converter",
                    navTransition = NavTransition(
                        createTransition = slideInHorizontally(initialOffsetX = {it/2}) + scaleIn(initialScale = 0.4f),
                        destroyTransition = scaleOut()
                    )
                ) {
                    ConverterScreen(
                        viewModel = converterViewModel,
                        onBack = { navigator.goBack() }
                    )
                }

                scene("/splitter",
                    navTransition = NavTransition(
                        createTransition = slideInHorizontally(initialOffsetX = {-it/2}) + scaleIn(initialScale = 0.4f),
                        destroyTransition = scaleOut()
                    )
                ) {
                    SplitterScreen(
                        viewModel = splitterViewModel,
                        onBack = { navigator.goBack() }
                    )
                }

                scene("/selectivesplitter",
                    navTransition = NavTransition(
                        createTransition = slideInHorizontally(initialOffsetX = {-it/4}) + scaleIn(initialScale = 0.4f),
                        destroyTransition = scaleOut()
                    )
                ) {
                    SelectiveSplitterScreen(
                        viewModel = selectiveSplitterViewModel,
                        onBack = { navigator.goBack() }
                    )
                }
            }
        }
    }
}