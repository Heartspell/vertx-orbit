plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform")
}

group = "com.heartspell.vertxorbit"
version = "0.9.6"

kotlin {
    jvmToolchain(21)
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2025.2") {
            useInstaller = false
        }
        bundledPlugin("org.jetbrains.kotlin")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        name = "Vert.x Orbit"
        version = project.version.toString()

        ideaVersion {
            sinceBuild = "252"
        }

        changeNotes = "First build"
    }
}
