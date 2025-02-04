import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.multiplatform)
}

java {
    sourceCompatibility = JavaVersion.VERSION_18
    targetCompatibility = JavaVersion.VERSION_18
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.common)

                implementation(compose.material3)

                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)

                implementation(libs.compose.navigation)

                implementation(libs.compose.material.icons)
                implementation(libs.compose.material.icons.extended)

                implementation(compose.components.uiToolingPreview)

                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtime.compose)

                implementation(libs.kotlin.serialization)

                implementation(libs.bundles.ktor)
            }
        }
    }
}

compose.desktop {
    application {
        buildTypes.release.proguard {
            obfuscate.set(true)

            configurationFiles.from(project.file("proguard-rules.pro"))
        }

        mainClass = "me.budchirp.plak.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "plak"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("icons")
            macOS {
                iconFile.set(iconsRoot.resolve("icon-macos.icns"))
            }

            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                menuGroup = "plak"
                upgradeUuid = "297b565f-9f0d-40e6-9116-7ea95f71cac7"
            }

            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }
        }
    }
}