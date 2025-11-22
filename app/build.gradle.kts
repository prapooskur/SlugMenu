@file:Suppress("UNUSED_EXPRESSION")

import com.android.build.api.dsl.Packaging
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")

    //KSP for room
    id("com.google.devtools.ksp")

    //Serialization
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = "com.pras.slugmenu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pras.slugmenu"
        minSdk = 24
        targetSdk = 36
        versionCode = 25
        versionName = "1.4.1"

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

            // better native debugging?
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
    }

    compileOptions {
        // Desugaring (allows minSdk below 26)
        isCoreLibraryDesugaringEnabled = true

        //Version 17 required by KSP
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

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


    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.activity:activity-ktx:1.12.0")
    implementation("androidx.activity:activity-compose:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    val composeUiVersion = "1.9.5"

    implementation("androidx.compose.ui:ui:$composeUiVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeUiVersion")
    implementation("androidx.compose.material:material:$composeUiVersion")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeUiVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeUiVersion")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$composeUiVersion")

    // jsoup HTML parser library @ https://jsoup.org/
    implementation("org.jsoup:jsoup:1.21.2")

    // ktor for http requests
    val ktorVersion = "3.3.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    // Android navigation
    // upgrading to compose 1.7 should have fixed bug?
    implementation("androidx.navigation:navigation-compose:2.9.6")

    // Material 3
    val mat3Version = "1.4.0"
    implementation("androidx.compose.material3:material3:$mat3Version")
    implementation("androidx.compose.material3:material3-window-size-class:$mat3Version")

    // manually added after removal in mat3 1.4.0
    implementation("androidx.compose.material:material-icons-core:1.7.8")

    //kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

    // Save settings
    implementation("androidx.datastore:datastore-preferences:1.2.0")

    // Colored Splash Screen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Room (caching implementation)
    val roomVersion = "2.8.4"

    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$roomVersion")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$roomVersion")

    // Flow stuff
    val coroutineVersion = "1.10.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutineVersion")

    // Desugaring (allows minSdk below 26)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    // WorkManager (allows app to download data in background)
    val workVersion = "2.11.0"
    implementation("androidx.work:work-runtime:$workVersion")
    // Kotlin + coroutines - no longer needed with workmanager 2.9.0?
    // implementation("androidx.work:work-runtime-ktx:$workVersion")

    // drag and droppable lists
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

    // accompanist for easier permission management
    implementation("com.google.accompanist:accompanist-permissions:0.37.3")

    // adaptive ui
    implementation("androidx.compose.material3.adaptive:adaptive:1.2.0")
    implementation("androidx.compose.material3.adaptive:adaptive-layout:1.2.0")
    implementation("androidx.compose.material3.adaptive:adaptive-navigation:1.2.0")
    implementation("com.google.accompanist:accompanist-adaptive:0.37.3")

    // viewModelScope
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
}


