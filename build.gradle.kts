// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.2.0-alpha16" apply false
    id("com.android.library") version "8.2.0-alpha16" apply false
    id("org.jetbrains.kotlin.android") version "1.8.22" apply false

    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.22" apply false

    //KSP for room
    id("com.google.devtools.ksp") version "1.8.22-1.0.11" apply false
}

