plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.seven.module_reviews"
    compileSdk = properties["compileSdk"].toString().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    implementation(project(":lib_core"))
    implementation(project(":lib_common"))
    implementation(project(":data_common"))
    implementation(project(":data_reviews"))

    ksp(libs.the.router.apt)
    implementation(libs.the.router)
}