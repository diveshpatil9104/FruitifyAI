plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jetbrains.kotlin.kapt")

}

android {
    namespace = "com.example.fruitifyai"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.fruitifyai"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
configurations.all {
    resolutionStrategy {
        force(
            "org.jetbrains.kotlin:kotlin-stdlib:1.9.0",
            "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.0",
            "org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.9.0",
            "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0"
        )
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
//    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // ✅ CameraX Core
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation ("com.airbnb.android:lottie-compose:6.4.0")
    implementation ("androidx.compose.material3:material3:1.3.2")


// database
    implementation ("androidx.room:room-runtime:2.7.1")
    implementation(libs.androidx.junit.ktx)
    implementation(libs.core.ktx)
    kapt ("androidx.room:room-compiler:2.7.1")
    implementation ("androidx.room:room-ktx:2.7.1")

    implementation ("org.tensorflow:tensorflow-lite:2.13.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.3")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.13.0") // optional for faster inference
    implementation ("org.pytorch:pytorch_android:1.13.1") // or latest version
    implementation ("org.pytorch:pytorch_android_torchvision:1.13.1")
    implementation(libs.play.services.basement)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}