import com.android.build.api.dsl.Packaging

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    //KSP for room
    id("com.google.devtools.ksp")

    //Serialization
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.pras.slugmenu"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.pras.slugmenu"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "selfbuilt"

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
            isShrinkResources  = true

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
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.8"
    }
    fun Packaging.() {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// for Room KSP
ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}

dependencies {


    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2")
    implementation("androidx.activity:activity-compose:1.7.2")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    val compose_ui_version = "1.4.3"

    implementation("androidx.compose.ui:ui:$compose_ui_version")
    implementation("androidx.compose.ui:ui-tooling-preview:$compose_ui_version")
    implementation("androidx.compose.material:material:$compose_ui_version")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-tooling:$compose_ui_version")
    debugImplementation("androidx.compose.ui:ui-test-manifest:$compose_ui_version")

    // jsoup HTML parser library @ https://jsoup.org/
    implementation("org.jsoup:jsoup:1.16.1")

    // ktor for http requests
    val ktor_version = "2.3.0"
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-websockets:$ktor_version")

    // Android navigation and Material 3

    val nav_version = "2.6.0"
    implementation("androidx.navigation:navigation-compose:$nav_version")

    val mat3_version = "1.1.1"
    implementation("androidx.compose.material3:material3:$mat3_version")
    implementation("androidx.compose.material3:material3-window-size-class:$mat3_version")

    // Accompanist (unofficial official Google libraries)
    // swipable tabs
    implementation("com.google.accompanist:accompanist-pager-indicators:0.30.1")
    // changing status bar color
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.30.1")
    // animations
    implementation("com.google.accompanist:accompanist-navigation-animation:0.30.1")

    //kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")

    // Save settings?
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Colored Splash Screen
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Room (caching implementation)
    val room_version = "2.5.2"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")

    // Flow stuff
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // Desugaring (allows minSdk below 26)
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // WorkManager (allows app to download data in background)
    val work_version = "2.8.1"
    // (Java only)
    implementation("androidx.work:work-runtime:$work_version")
    // Kotlin + coroutines
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // drag and droppable lists
    implementation("org.burnoutcrew.composereorderable:reorderable:0.9.6")

}


