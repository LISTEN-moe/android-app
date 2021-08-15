plugins {
    id("com.android.application") version "7.0.0" apply false
    id("com.android.library") version "7.0.0" apply false
    kotlin("android") version "1.5.21" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("com.github.ben-manes.versions") version "0.33.0"
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven { setUrl("https://www.jitpack.io") }
        maven { setUrl("https://plugins.gradle.org/m2/") }
    }
}

subprojects {
    apply {
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    ktlint {
        debug.set(false)
        version.set("0.42.1")
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(false)
//        enableExperimentalRules.set(true)
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
