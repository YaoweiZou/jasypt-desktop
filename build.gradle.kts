import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "1.8.20"
    id("org.jetbrains.compose") version "1.4.1"
}

group = "com.yaoweizou"
version = "1.0.3"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)

    // https://mvnrepository.com/artifact/org.jasypt/jasypt
    implementation("org.jasypt:jasypt:1.9.3")
}

compose.desktop {
    application {
        mainClass = "com.yaoweizou.MainKt"

        buildTypes.release.proguard {
            configurationFiles.from(project.file("proguard.cfg"))
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "Jasypt Desktop"
            packageVersion = "1.0.3"
            description = "Jasypt encryption/decryption tools."
            copyright = "2023 Yaowei Zou. MIT License."

            windows {
                iconFile.set(project.file("src/main/resources/icons/icon.ico"))
                shortcut = true
                dirChooser = true
            }

            macOS {
                dockName = "Jasypt Desktop"
                iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            }

            linux {
                iconFile.set(project.file("src/main/resources/icons/icon.png"))
            }
        }
    }
}
