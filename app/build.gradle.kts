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
        
        // Google Cloud Speech-to-Text API Key
        buildConfigField("String", "GOOGLE_SPEECH_API_KEY", "\"${localProperties["GOOGLE_SPEECH_API_KEY"] ?: "b5adb7fe5d424661928c6ee323684480"}\"")
        
        // Mapbox Access Token (for MapTiler integration)
        manifestPlaceholders["MAPBOX_ACCESS_TOKEN"] = localProperties["MAPBOX_ACCESS_TOKEN"] ?: "YOUR_MAPBOX_TOKEN_HERE"
        buildConfigField("String", "MAPTILER_API_KEY", "\"${localProperties["MAPTILER_API_KEY"] ?: "bUMv21mRxmb69YiXSLFS"}\"")
        
        // Legacy Google Maps API Key (if needed elsewhere)
        manifestPlaceholders["MAPS_API_KEY"] = localProperties["MAPS_API_KEY"] ?: "YOUR_API_KEY_HERE"
        
        // Roboflow API Configuration - 4 Detection Models
        // HALL Detection
        buildConfigField("String", "ROBOFLOW_HALL_URL", "\"${localProperties["ROBOFLOW_HALL_URL"] ?: ""}\"")
        buildConfigField("String", "ROBOFLOW_HALL_API_KEY", "\"${localProperties["ROBOFLOW_HALL_API_KEY"] ?: ""}\"")
        
        // DOORS Detection
        buildConfigField("String", "ROBOFLOW_DOORS_URL", "\"${localProperties["ROBOFLOW_DOORS_URL"] ?: ""}\"")
        buildConfigField("String", "ROBOFLOW_DOORS_API_KEY", "\"${localProperties["ROBOFLOW_DOORS_API_KEY"] ?: ""}\"")
        
        // WINDOWS Detection
        buildConfigField("String", "ROBOFLOW_WINDOWS_URL", "\"${localProperties["ROBOFLOW_WINDOWS_URL"] ?: ""}\"")
        buildConfigField("String", "ROBOFLOW_WINDOWS_API_KEY", "\"${localProperties["ROBOFLOW_WINDOWS_API_KEY"] ?: ""}\"")
        
        // STAIRS Detection
        buildConfigField("String", "ROBOFLOW_STAIRS_URL", "\"${localProperties["ROBOFLOW_STAIRS_URL"] ?: ""}\"")
        buildConfigField("String", "ROBOFLOW_STAIRS_API_KEY", "\"${localProperties["ROBOFLOW_STAIRS_API_KEY"] ?: ""}\"")
        
        // Legacy Roboflow Keys (for backward compatibility)
        buildConfigField("String", "RF_WINDOWS_KEY", "\"${localProperties["RF_WINDOWS_KEY"] ?: localProperties["ROBOFLOW_WINDOWS_API_KEY"] ?: ""}\"")
        buildConfigField("String", "RF_DOOR_KEY", "\"${localProperties["RF_DOOR_KEY"] ?: localProperties["ROBOFLOW_DOORS_API_KEY"] ?: ""}\"")
        buildConfigField("String", "RF_HALL_KEY", "\"${localProperties["RF_HALL_KEY"] ?: localProperties["ROBOFLOW_HALL_API_KEY"] ?: ""}\"")
        buildConfigField("String", "RF_STAIRS_KEY", "\"${localProperties["RF_STAIRS_KEY"] ?: localProperties["ROBOFLOW_STAIRS_API_KEY"] ?: ""}\"")
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
    
    // Mapbox Maps SDK (for MapTiler integration)
    implementation("com.mapbox.maps:android:11.0.0")
    
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