import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.util.UUID
import org.jetbrains.compose.reload.ComposeHotRun 
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag
import java.util.Scanner

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.plugin.serialization)
    alias(libs.plugins.hotReload)
}

group = "zahid4kh.sumpdf"
version = "1.2.1"

repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.swing)

    // Koin for dependency injection
    implementation(libs.koin.core)

    // PDFbox for merger utility
    implementation("org.apache.pdfbox:pdfbox:3.0.3")
    implementation("commons-logging:commons-logging:1.3.5")

    implementation("com.github.zahid4kh:deskit:1.2.1")

    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("org.slf4j:slf4j-simple:2.0.12")

    implementation("org.jodconverter:jodconverter-local:4.4.8")
    implementation("org.jodconverter:jodconverter-core:4.4.8")

    implementation("org.apache.xmlgraphics:batik-all:1.18")

    api("moe.tlaster:precompose:1.6.2")
    api("moe.tlaster:precompose-viewmodel:1.6.2")
    api(compose.foundation)
    api(compose.animation)

    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}

compose.resources{
    generateResClass = auto
    packageOfResClass = "sumpdf.resources"
    publicResClass = false
}

compose.desktop {
    application {
        mainClass = "SumPDF"

        nativeDistributions {
            jvmArgs += listOf(
                "-Dfile.encoding=UTF-8",
                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED"
            )
            buildTypes.release.proguard {
                configurationFiles.from("proguard-rules.pro")
                isEnabled = true
                obfuscate = false
                optimize = false
            }

            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "sumpdf"
            packageVersion = "1.2.1"

            linux{
                shortcut = true
                iconFile.set(project.file("icons/sumpdf.png"))
                description = "Tool for combining PDFs into one and converting various file types into PDF format"
            }

            windows{
                shortcut = true
                dirChooser = true
                menu = true
                vendor = "Zahid Khalilov"
                description = "Tool for combining PDFs into one and converting various file types into PDF format"
                upgradeUuid = "6414014c-b5f0-462e-967f-14fdd38386f5"
                iconFile.set(project.file("icons/sumpdf.ico"))
            }

            macOS{
                dockName = "SumPDF"
                iconFile.set(project.file("icons/sumpdf.icns"))
            }
        }
    }
}

tasks.register("generateBuildConfig") {
    group = "build"
    description = "Generates BuildConfig.kt with project version"

    doLast {
        val buildConfigDir = file("src/main/kotlin/sumpdf")
        buildConfigDir.mkdirs()

        val buildConfigFile = file("$buildConfigDir/BuildConfig.kt")
        val projectVersion = project.version.toString()

        buildConfigFile.writeText("""
            package sumpdf
            
            object BuildConfig {
                const val VERSION_NAME = "$projectVersion"
            }
        """.trimIndent())

        println("Generated BuildConfig.kt with VERSION_NAME = $projectVersion")
    }
}

tasks.named("compileKotlin") {
    dependsOn("generateBuildConfig")
}

tasks.named("generateBuildConfig") {
    inputs.property("version", project.version)
    outputs.file(file("src/main/kotlin/sumpdf/BuildConfig.kt"))
}

//https://github.com/JetBrains/compose-hot-reload
composeCompiler {
    featureFlags.add(ComposeFeatureFlag.OptimizeNonSkippingGroups)
}

tasks.register("generateUpgradeUuid") {
    group = "help"
    description = "Generates a unique UUID to be used for the Windows MSI upgradeUuid."
    doLast {
        println("--------------------------------------------------")
        println("Generated Upgrade UUID (must be pasted in the upgradeUuid for windows block only once so the MSI installer recognizes the update and does the uninstall/install):")
        println(UUID.randomUUID().toString())
        println("--------------------------------------------------")
    }
}


// only for LinuxOS
val workDir = file("deb-temp")
val packageName = "${compose.desktop.application.nativeDistributions.packageName}"
val desktopRelativePath = "opt/$packageName/lib/$packageName-$packageName.desktop"
val appDisplayName = "SumPDF"
val mainClass = "${compose.desktop.application.mainClass}"
val maintainer = "Zahid Khalilov <halilzahid@gmail.com>"
val controlDescription = "Tool for combining PDFs into one and converting various file types into PDF format"

fun promptUserChoice(): String {
    println(
        """
        🧩 Which packaging task do you want to run?
        1 = packageDeb (default)
        2 = packageReleaseDeb
        """.trimIndent()
    )
    print("👉 Enter your choice [1/2]: ")

    return Scanner(System.`in`).nextLine().trim().ifEmpty { "1" }
}

tasks.register("addStartupWMClassToDebDynamic") {
    group = "release"
    description = "Finds .deb file, modifies .desktop and control files, and rebuilds it"

    doLast {
        val debRoot = file("build/compose/binaries")
        if (!debRoot.exists()) throw GradleException("❌ Folder not found: ${debRoot}")

        val allDebs = debRoot.walkTopDown().filter { it.isFile && it.extension == "deb" }.toList()
        if (allDebs.isEmpty()) throw GradleException("❌ No .deb files found under ${debRoot}")

        // picking the latest .deb file
        val originalDeb = allDebs.maxByOrNull { it.lastModified() }!!
        println("📦 Found deb package: ${originalDeb.relativeTo(rootDir)}")

        val modifiedDeb = File(originalDeb.parentFile, originalDeb.nameWithoutExtension + "-wm.deb")

        // cleaning up "deb-temp" folder, if exists
        if (workDir.exists()) workDir.deleteRecursively()
        workDir.mkdirs()

        // Step 1: Extracting generated debian package
        exec {
            commandLine("dpkg-deb", "-R", originalDeb.absolutePath, workDir.absolutePath)
        }

        // Step 2: Modifying the desktop entry file
        val desktopFile = File(workDir, desktopRelativePath)
        if (!desktopFile.exists()) throw GradleException("❌ .desktop file not found: ${desktopRelativePath}")

        val lines = desktopFile.readLines().toMutableList()

        // Modifying the Name field (app's display name on dock)
        var nameModified = false
        for (i in lines.indices) {
            if (lines[i].trim().startsWith("Name=")) {
                lines[i] = "Name=$appDisplayName"
                nameModified = true
                println("✅ Modified Name entry to: $appDisplayName")
                break
            }
        }

        // adding Name field if it doesn't exist
        if (!nameModified) {
            lines.add("Name=$appDisplayName")
            println("✅ Added Name entry: $appDisplayName")
        }

        for (i in lines.indices) {
            if (lines[i].trim().startsWith("StartupWMClass=")) {
                if (lines[i] != "StartupWMClass=$mainClass") {
                    lines[i] = "StartupWMClass=$mainClass"
                    println("✅ Updated StartupWMClass entry to: $mainClass")
                } else {
                    println("ℹ️ StartupWMClass already correctly set to: $mainClass")
                }
                break
            }
        }

        // Adding StartupWMClass if it doesn't exist
        if (!lines.any { it.trim().startsWith("StartupWMClass=") }) {
            lines.add("StartupWMClass=$mainClass")
            println("✅ Added StartupWMClass entry: $mainClass")
        }

        // Writing changes back to file
        desktopFile.writeText(lines.joinToString("\n"))

        println("\n📄 Final .desktop file content:")
        println("--------------------------------")
        desktopFile.readLines().forEach { println(it) }
        println("--------------------------------\n")

        // Step 3: Modifying the DEBIAN/control file
        val controlFile = File(workDir, "DEBIAN/control")
        if (!controlFile.exists()) throw GradleException("❌ control file not found: DEBIAN/control")

        val controlLines = controlFile.readLines().toMutableList()

        // Update maintainer field
        var maintainerModified = false
        for (i in controlLines.indices) {
            if (controlLines[i].trim().startsWith("Maintainer:")) {
                controlLines[i] = "Maintainer: $maintainer"
                maintainerModified = true
                println("✅ Modified Maintainer entry")
                break
            }
        }

        // Add maintainer field if it doesn't exist
        if (!maintainerModified) {
            controlLines.add("Maintainer: $maintainer")
            println("✅ Added Maintainer entry")
        }

        // Update description field for better info
        for (i in controlLines.indices) {
            if (controlLines[i].trim().startsWith("Description:")) {
                controlLines[i] = "Description: $controlDescription"
                println("✅ Modified Description entry")
                break
            }
        }

        // Write changes back to control file
        controlFile.writeText(controlLines.joinToString("\n"))

        println("\n📄 Final control file content:")
        println("--------------------------------")
        controlFile.readLines().forEach { println(it) }
        println("--------------------------------\n")

        // Step 4: Repackaging the debian package back
        exec {
            commandLine("dpkg-deb", "-b", workDir.absolutePath, modifiedDeb.absolutePath)
        }

        println("✅ Done: Rebuilt with Name=$appDisplayName, StartupWMClass=$mainClass, and updated control file -> ${modifiedDeb.name}")
    }
}


tasks.register("packageDebWithWMClass") {
    group = "release"
    description = "Runs packaging task (packageDeb or packageReleaseDeb), then adds StartupWMClass"

    doLast {
        val choice = promptUserChoice()

        val packagingTask = when (choice) {
            "2" -> "packageReleaseDeb"
            else -> "packageDeb"
        }

        println("▶️ Running: ${packagingTask}")
        gradle.includedBuilds.forEach { it.task(":${packagingTask}") } // just in case of composite builds

        exec {
            commandLine("./gradlew clean")
            commandLine("./gradlew", packagingTask)
        }

        tasks.named("addStartupWMClassToDebDynamic").get().actions.forEach { it.execute(this) }
    }
}