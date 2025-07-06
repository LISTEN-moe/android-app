import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.aboutLibraries)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.autoresconfig)
}

val appPackageName = "me.echeung.moemoekyun"

android {
    compileSdk = 35
    namespace = appPackageName

    defaultConfig {
        applicationId = appPackageName
        minSdk = 26
        targetSdk = 35
        versionCode = 209
        versionName = "6.2.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true

        // Disable unused AGP features
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
    }

    autoResConfig {
        generateClass = true
        generateRes = true
        generateLocaleConfig = true
    }

    buildTypes {
        named("debug") {
            applicationIdSuffix = ".debug"
            versionNameSuffix = " DEBUG"
        }
        named("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    flavorDimensions.add("variant")
    productFlavors {
        create("playstore") {
            dimension = "variant"
        }
        create("fdroid") {
            dimension = "variant"

            applicationIdSuffix = ".fdroid"
        }
    }

    lint {
        disable.addAll(listOf("MissingTranslation", "ExtraTranslation"))
        enable.addAll(listOf("ObsoleteSdkInt"))

        abortOnError = true
    }

    packaging {
        resources.excludes.addAll(
            listOf(
                "META-INF/DEPENDENCIES",
                "LICENSE.txt",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/README.md",
                "META-INF/NOTICE",
                "META-INF/*.kotlin_module",
                "META-INF/*.version",
            ),
        )
    }

    dependenciesInfo {
        includeInApk = false
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

val jvmVersion = JavaLanguageVersion.of(17)
java {
    toolchain {
        languageVersion.set(jvmVersion)
    }
}
kotlin {
    jvmToolchain(jvmVersion.asInt())

    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
        )
    }
}

apollo {
    service("service") {
        packageName.set(appPackageName)
    }
}

ktlint {
    filter {
        exclude { element ->
            element.file.path.contains("generated/")
        }
    }
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        }
    }
}

dependencies {
    coreLibraryDesugaring(libs.desugar)
    implementation(libs.bundles.coroutines)
    implementation(libs.serialization)
    implementation(libs.immutables)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.bundles.okhttp)
    implementation(libs.bundles.coil)
    implementation(libs.bundles.apollo)
    implementation(libs.logcat)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.aboutLibraries.compose)
    lintChecks(libs.compose.lintchecks)

    implementation(libs.bundles.voyager)

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.splashscreen)

    implementation(libs.bundles.media)
    implementation(libs.bundles.preferences)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.kotest.assertions)
    testRuntimeOnly(libs.bundles.junit.runtime)

    // For detecting memory leaks; see https://square.github.io/leakcanary/
    // "debugImplementation"("com.squareup.leakcanary:leakcanary-android:2.2")
}
