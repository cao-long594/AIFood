plugins {
    alias(libs.plugins.android.application)
}

import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

fun buildConfigString(name: String): String {
    val value = localProperties.getProperty(name).orEmpty()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$value\""
}

android {
    namespace = "com.example.food"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.food"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "FOOD_RECOGNITION_API_URL", buildConfigString("foodRecognition.apiUrl"))
            buildConfigField("String", "FOOD_RECOGNITION_API_KEY", buildConfigString("foodRecognition.apiKey"))
            buildConfigField("String", "FOOD_RECOGNITION_MODEL", buildConfigString("foodRecognition.model"))
        }
        release {
            isMinifyEnabled = false
            buildConfigField("String", "FOOD_RECOGNITION_API_URL", buildConfigString("foodRecognition.apiUrl"))
            buildConfigField("String", "FOOD_RECOGNITION_API_KEY", buildConfigString("foodRecognition.apiKey"))
            buildConfigField("String", "FOOD_RECOGNITION_MODEL", buildConfigString("foodRecognition.model"))
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.viewpager2)

    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    implementation(libs.mpandroidchart)
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    implementation(libs.okhttp)
    implementation(libs.gson)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
