plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.10" apply false
}

// Keep the Gradle daemon aligned with the JDK 17 used by Android Studio.
tasks.named<org.gradle.buildconfiguration.tasks.UpdateDaemonJvm>("updateDaemonJvm") {
    languageVersion = org.gradle.jvm.toolchain.JavaLanguageVersion.of(17)
}
