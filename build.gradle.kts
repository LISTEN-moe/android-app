buildscript {
    dependencies {
        classpath(libs.hilt.gradle)
        classpath(libs.kotlin.gradle)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
