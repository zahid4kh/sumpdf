import combiner.CombinerViewModel
import org.koin.dsl.module

val appModule = module {
    single { Database() }
    single { CombinerViewModel(get()) }
}