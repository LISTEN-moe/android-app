buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
        classpath("org.jetbrains.kotlin:kotlin-serialization:1.8.10")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.45")
    }
}

plugins {
    id("com.android.application") version "7.4.2" apply false
    id("com.android.library") version "7.4.2" apply false
    kotlin("android") version "1.8.10" apply false
    id("org.jmailen.kotlinter") version "3.11.1"
    id("com.mikepenz.aboutlibraries.plugin") version "10.6.1" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven(url = "https://www.jitpack.io")
        maven(url = "https://plugins.gradle.org/m2/")
    }
}

subprojects {
    apply<org.jmailen.gradle.kotlinter.KotlinterPlugin>()

    kotlinter {
        experimentalRules = true

        // Doesn't play well with Android Studio
        disabledRules = arrayOf("experimental:argument-list-wrapping")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
