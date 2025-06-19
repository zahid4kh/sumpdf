import combiner.CombinerViewModel
import converter.Converter
import converter.ConverterViewModel
import converter.PDFConverter
import selectivesplitter.SelectiveSplitterViewModel
import splitter.SplitterViewModel
import org.koin.dsl.module

val appModule = module {
    single { Database() }
    single<Converter> { PDFConverter() }
    single { CombinerViewModel(get()) }
    single { ConverterViewModel(get(), get()) }
    single { SplitterViewModel(get()) }
    single { SelectiveSplitterViewModel(get()) }
}