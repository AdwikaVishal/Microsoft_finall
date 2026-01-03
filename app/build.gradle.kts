import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34 

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26 
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load local.properties
        val localProperties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(FileInputStream(localPropertiesFile))
        }

        buildConfigField("String", "SPEECHTEXT_API_KEY", "\"${localProperties["SPEECHTEXT_API_KEY"] ?: ""}\"")
        buildConfigField("String", "AZURE_KEY", "\"${localProperties["AZURE_KEY"] ?: ""}\"")
        buildConfigField("String", "AZURE_ENDPOINT", "\"${localProperties["AZURE_ENDPOINT"] ?: ""}\"")
        buildConfigField("String", "LIBRE_TRANSLATE_BASE_URL", "\"${localProperties["LIBRE_TRANSLATE_BASE_URL"] ?: ""}\"")
        // 4 Model Roboflow API Keys
        buildConfigField("String", "RF_WINDOWS_KEY", "\"${localProperties["RF_WINDOWS_KEY"] ?: ""}\"")
        buildConfigField("String", "RF_DOOR_KEY", "\"${localProperties["RF_DOOR_KEY"] ?: ""}\"")
        buildConfigField("String", "RF_HALL_KEY", "\"${localProperties["RF_HALL_KEY"] ?: ""}\"")
        buildConfigField("String", "RF_STAIRS_KEY", "\"${localProperties["RF_STAIRS_KEY"] ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.google.gson)

    // Background work
    implementation(libs.androidx.work.runtime.ktx)

    // Location
    implementation(libs.play.services.location)
    
    // Coroutines support for Play Services (tasks)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // CameraX and ML Kit
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.com.google.mlkit.text.recognition)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Accompanist Permissions
    implementation(libs.com.google.accompanist.permissions)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}