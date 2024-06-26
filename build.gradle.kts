// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("com.android.library") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false

    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" apply false

    //KSP for room
    id("com.google.devtools.ksp") version "1.9.23-1.0.20" apply false
}