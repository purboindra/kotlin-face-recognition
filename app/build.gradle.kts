plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    alias(libs.plugins.hilt)
    alias(libs.plugins.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.imagerecognition"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.imagerecognition"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material:material-icons-extended:1.7.6")
    implementation(libs.play.services.mlkit.face.detection)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Tensorflow
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    // Camera
    implementation(libs.camera.core)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.camera2)
    implementation(libs.camera.view)

    // Navigation
    implementation(libs.navigation.compose)

    // Hilt
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)

    // Serialization
    implementation(libs.serialization.json)

    // ML Kit
    implementation("com.google.mlkit:object-detection:17.0.1")
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Google Services
    implementation(libs.google.services)
    implementation(platform(libs.google.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.firestore.ktx)
}

kapt {
    correctErrorTypes = true
}