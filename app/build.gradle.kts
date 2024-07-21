@file:Suppress("UNUSED_EXPRESSION")

import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")

    //KSP for room
    id("com.google.devtools.ksp")

    //Serialization
    id("org.jetbrains.kotlin.plugin.serialization")
}

composeCompiler {
    enableStrongSkippingMode = true
}

android {
    namespace = "com.pras.slugmenu"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.pras.slugmenu"
        minSdk = 24
        targetSdk = 34
        versionCode = 21
        versionName = "1.3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            // Enables code shrinking, obfuscation, and optimization for only
            // your project's release build type.
            isMinifyEnabled = true

            // Enables resource shrinking, which is performed by the
            // Android Gradle plugin.
            isShrinkResources = true

            // Includes the default ProGuard rules files that are packaged with
            // the Android Gradle plugin. To learn more, go to the section about
            // R8 configuration files.
            proguardFiles (
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Desugaring (allows minSdk below 26)
        isCoreLibraryDesugaringEnabled = true

        //Version 17 required by KSP
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
//    composeOptions {
//        kotlinCompilerExtensionVersion = "1.5.11"
//    }
    fun Packaging.() {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// for Room KSP
ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")

    // make room generate kotlin instead of java
    arg("room.generateKotlin", "true")
}



dependencies {


    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    val composeUiVersion = "1.7.0-beta05"

    implementation("androidx.compose.ui:ui:$composeUiVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation("androidx.compose.material:material:$composeUiVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    // jsoup HTML parser library @ https://jsoup.org/
    implementation("org.jsoup:jsoup:1.18.1")

    // ktor for http requests
    val ktorVersion = "2.3.12"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    // Android navigation
    // upgrading to compose 1.7 should have fixed bug?
    implementation("androidx.navigation:navigation-compose:2.8.0-beta05")

    // Material 3
    val mat3Version = "1.2.1"
    implementation("androidx.compose.material3:material3:$mat3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$mat3Version")

    //kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Save settings
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Colored Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Room (caching implementation)
    val roomVersion = "2.6.1"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$roomVersion")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")


    // Flow stuff
    val coroutineVersion = "1.8.1"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

    // Desugaring (allows minSdk below 26)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // WorkManager (allows app to download data in background)
    val workVersion = "2.9.0"
    implementation("androidx.work:work-runtime:$workVersion")
    // Kotlin + coroutines - no longer needed with workmanager 2.9.0?
    // implementation("androidx.work:work-runtime-ktx:$workVersion")

    // drag and droppable lists
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // accompanist for easier permission management
    implementation("com.google.accompanist:accompanist-permissions:0.35.1-alpha")

    // adaptive ui
    implementation("androidx.compose.material3.adaptive:adaptive:1.0.0-beta04")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.0.0-beta04")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.0.0-beta04")
    implementation("com.google.accompanist:accompanist-adaptive:0.35.1-alpha")
}


