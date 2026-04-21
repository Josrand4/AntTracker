plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.antracker"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.antracker"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(libs.recyclerview)

    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)

    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    implementation(libs.mpandroidchart)

    // Firebase BOM (maneja versiones automáticamente)
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    
    // Firestore (base de datos)
    implementation("com.google.firebase:firebase-firestore")
    
    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.5.1")

    // Glide para cargar imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}