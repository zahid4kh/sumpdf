import combiner.AppSettings
import converter.ConversionTask
import converter.ConverterSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class Database {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val appDir: File
    private val settingsFile: File
    private val converterSettingsFile: File
    private val tasksFile: File

    init {
        val userHome = System.getProperty("user.home")
        appDir = File(userHome, ".sumpdf").apply {
            if (!exists()) mkdirs()
        }

        settingsFile = File(appDir, "settings.json")
        converterSettingsFile = File(appDir, "converter_settings.json")
        tasksFile = File(appDir, "tasks.json")

        if (!settingsFile.exists()) settingsFile.writeText(json.encodeToString(AppSettings.serializer(), AppSettings()))
        if (!converterSettingsFile.exists()) converterSettingsFile.writeText(json.encodeToString(ConverterSettings.serializer(), ConverterSettings()))
        if (!tasksFile.exists()) tasksFile.writeText("[]")
    }

    suspend fun getSettings(): AppSettings = withContext(Dispatchers.IO) {
        return@withContext try {
            json.decodeFromString(AppSettings.serializer(), settingsFile.readText())
        } catch (e: Exception) {
            AppSettings()
        }
    }

    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsFile.writeText(json.encodeToString(AppSettings.serializer(), settings))
    }

    suspend fun getConverterSettings(): ConverterSettings = withContext(Dispatchers.IO) {
        return@withContext try {
            json.decodeFromString(ConverterSettings.serializer(), converterSettingsFile.readText())
        } catch (e: Exception) {
            ConverterSettings()
        }
    }

    suspend fun saveConverterSettings(settings: ConverterSettings) = withContext(Dispatchers.IO) {
        converterSettingsFile.writeText(json.encodeToString(ConverterSettings.serializer(), settings))
    }

    suspend fun getTasks(): List<ConversionTask> = withContext(Dispatchers.IO) {
        return@withContext try {
            json.decodeFromString(tasksFile.readText())
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTasks(tasks: List<ConversionTask>) = withContext(Dispatchers.IO) {
        tasksFile.writeText(json.encodeToString(tasks))
    }
}