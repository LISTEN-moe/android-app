import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.android.application)
    id("com.mikepenz.aboutlibraries.plugin")
    kotlin("android")
    kotlin("plugin.serialization")
    id("kotlin-parcelize")
    id("dagger.hilt.android.plugin")
    alias(libs.plugins.ksp)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.autoresconfig)
}

val appPackageName = "me.echeung.moemoekyun"

android {
    compileSdk = 34
    namespace = appPackageName

    defaultConfig {
        applicationId = appPackageName
        minSdk = 26
        targetSdk = 34
        versionCode = 206
        versionName = "6.0.2"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.version.get()
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
    implementation(libs.androidx.media)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.splashscreen)

    implementation(libs.bundles.preferences)

    // For detecting memory leaks; see https://square.github.io/leakcanary/
    // "debugImplementation"("com.squareup.leakcanary:leakcanary-android:2.2")
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
    withType<KotlinCompile> {
        // See https://kotlinlang.org/docs/reference/experimental.html#experimental-status-of-experimental-api-markers
        kotlinOptions.freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-opt-in=kotlin.Experimental",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview",
            "-opt-in=kotlinx.serialization.ExperimentalSerializationApi",
        )
    }
}
