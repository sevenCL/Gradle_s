// Top-level build file where you can add configuration options common to all sub-projects/modules.
extra.apply {
    set("applicationId", "com.seven.gradle")
    set("compileSdk", 34)
    set("minSdk", 28)
    set("targetSdk", 34)
    set("versionCode", 1)
    set("versionName", "1.0.0")
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.the.router) apply false
}