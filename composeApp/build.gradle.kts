plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    js {
        browser()
        binaries.executable()
    }

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

val buildNativeLib by tasks.registering(Exec::class) {
    val buildDir = layout.buildDirectory.dir("native").get().asFile
    val sourceDir = file("src/commonMain/cpp")

    doFirst { buildDir.mkdirs() }

    commandLine(
        "cmake",
        "-S", sourceDir.absolutePath,
        "-B", buildDir.absolutePath,
        //"-G", "Visual Studio 17 2022"
    )
}

val buildNativeRelease by tasks.registering(Exec::class) {
    dependsOn(buildNativeLib)

    val buildDir = layout.buildDirectory.dir("native").get().asFile

    if (org.gradle.internal.os.OperatingSystem.current().isWindows()) {
        commandLine(
            "cmake",
            "--build", buildDir.absolutePath,
            "--config", "Release"
        )
    } else {
        commandLine(
            "cmake",
            "--build", buildDir.absolutePath
        )
    }
}

val copyNativeLib by tasks.registering(Copy::class) {
    dependsOn(buildNativeRelease)

    val osName = org.gradle.internal.os.OperatingSystem.current().let {
        when {
            it.isWindows() -> "win32-x86-64"
            it.isLinux() -> "linux-x86-64"
            it.isMacOsX() -> "darwin-x86-64"
            else -> "unknown"
        }
    }

    val libName = if (org.gradle.internal.os.OperatingSystem.current().isWindows()) {
        "Release/implicit_graph.dll"
    } else {
        "libimplicit_graph.so"
    }

    from(layout.buildDirectory.dir("native/$libName"))
    into("src/jvmMain/resources/$osName/")
}

tasks.named("jvmProcessResources") {
    dependsOn(copyNativeLib)
}

tasks.named("build") {
    dependsOn(copyNativeLib)
}

compose.desktop {
    application {
        mainClass = "org.calculator.MainKt"
        nativeDistributions {
            targetFormats(org.jetbrains.compose.desktop.application.dsl.TargetFormat.Dmg, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Msi, org.jetbrains.compose.desktop.application.dsl.TargetFormat.Deb)
            packageName = "org.calculator"
            packageVersion = "1.0.0"
        }
    }
}

