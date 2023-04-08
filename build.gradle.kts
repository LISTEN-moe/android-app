buildscript {
    dependencies {
        classpath(libs.hilt.gradle)
        classpath(libs.aboutLibraries.gradle)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.serialization) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlinter) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
