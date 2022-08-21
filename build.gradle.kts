buildscript {
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
    }
}

plugins {
    id("com.android.application") version "7.0.2" apply false
    id("com.android.library") version "7.0.2" apply false
    kotlin("android") version "1.6.21" apply false
    id("org.jmailen.kotlinter") version "3.6.0"
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
