import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }
    
    /*@OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }*/
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("net.java.dev.jna:jna:5.13.0")
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.calculator.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.calculator"
            packageVersion = "1.0.0"
        }
    }
}



val buildNativeLib by tasks.registering(Exec::class) {
    val buildDir = layout.buildDirectory.dir("native").get().asFile
    val sourceDir = file("src/commonMain/cpp")

    // Ensure build directory exists
    doFirst { buildDir.mkdirs() }

    // Configure CMake
    commandLine(
        "cmake",
        "-S", sourceDir.absolutePath,
        "-B", buildDir.absolutePath,
        "-G", "Visual Studio 17 2022"
    )
}

val buildNativeRelease by tasks.registering(Exec::class) {
    val buildDir = layout.buildDirectory.dir("native").get().asFile

    dependsOn(buildNativeLib)

    commandLine(
        "cmake",
        "--build", buildDir.absolutePath,
        "--config", "Release"
    )
}

val copyNativeLib by tasks.registering(Copy::class) {
    dependsOn(buildNativeRelease)

    from("build/native/Release/implicit_graph.dll")
    into("src/jvmMain/resources/win32-x86-64/")
}

tasks.named("jvmProcessResources") {
    dependsOn(copyNativeLib)
}

tasks.named("jvmProcessResources") {
    dependsOn(copyNativeLib)
}
tasks.named("build") {
    dependsOn(copyNativeLib)
}

