import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("com.mikepenz.aboutlibraries.plugin")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("dagger.hilt.android.plugin")
    id("com.apollographql.apollo3") version "3.7.3"
}

val appPackageName = "me.echeung.moemoekyun"

android {
    compileSdk = 33
    namespace = appPackageName

    defaultConfig {
        applicationId = appPackageName
        minSdk = 23
        targetSdk = 33
        versionCode = 200
        versionName = "6.0.0-beta.1"
    }

    buildFeatures {
        compose = true

        // Disable unused AGP features
        aidl = false
        renderScript = false
        resValues = false
        shaders = false
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

    packagingOptions {
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.4"
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.2")

    val hiltVersion = "2.45"
    implementation("com.google.dagger:hilt-android:$hiltVersion")
    kapt("com.google.dagger:hilt-compiler:$hiltVersion")

    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.ui:ui-util")
    implementation("androidx.activity:activity-compose:1.7.0")

    val voyagerVersion = "1.0.0-rc03"
    implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
    implementation("cafe.adriel.voyager:voyager-hilt:$voyagerVersion")

    val coroutinesVersion = "1.6.4"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    implementation("io.insert-koin:koin-android:3.2.0")

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")
    implementation("androidx.media:media:1.6.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("androidx.preference:preference-ktx:1.2.0")

    implementation("com.github.tfcporciuncula:flow-preferences:1.3.4")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")

    val apolloVersion = "3.7.3"
    implementation("com.apollographql.apollo3:apollo-runtime:$apolloVersion")
    implementation("com.apollographql.apollo3:apollo-http-cache:$apolloVersion")

    val coilVersion = "2.3.0"
    implementation("io.coil-kt:coil:$coilVersion")
    implementation("io.coil-kt:coil-gif:$coilVersion")
    implementation("io.coil-kt:coil-compose:$coilVersion")

    val exoplayerVersion = "2.18.1"
    implementation("com.google.android.exoplayer:exoplayer-core:$exoplayerVersion")

    implementation("com.mikepenz:aboutlibraries-compose:10.6.1")

    implementation("com.squareup.logcat:logcat:0.1")

    // For detecting memory leaks; see https://square.github.io/leakcanary/
    // "debugImplementation"("com.squareup.leakcanary:leakcanary-android:2.2")
}

apollo {
    packageName.set(appPackageName)
}

kapt {
    correctErrorTypes = true
}

tasks {
    val localesConfigTask = registerLocalesConfigTask(project)

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

    preBuild {
        dependsOn(formatKotlin, localesConfigTask)
    }
}
