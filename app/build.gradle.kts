plugins {
    // 🔹 Plugins declarados en libs.versions.toml
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)

    // 🆕 Plugin de Google Services para Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.jorge.inmobiliaria2025"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jorge.inmobiliaria2025"
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

    // ✅ Fuerza compilación con Java 17
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // ✅ Sincroniza Kotlin con la misma JVM
    kotlin {
        jvmToolchain(17)
    }

    // 🧠 Habilitar ViewBinding
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)

    implementation(libs.activity)
    implementation(libs.fragment)

    // 🖼️ Glide (carga de imágenes)
    implementation(libs.glide)
    kapt(libs.glide.compiler)

    // 🧭 Navigation Component
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)

    // 🏠 Room + LiveData
    implementation(libs.lifecycle.livedata)
    implementation(libs.room.runtime)
    kapt(libs.room.compiler)

    // 🗺️ Google Maps y ubicación
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    // 🔹 JSON serializer
    implementation(libs.gson)

    // 🌐 Retrofit + OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // ✅ Dependencias de prueba
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // 🆕 Firebase Cloud Messaging (para notificaciones push)
    implementation(platform("com.google.firebase:firebase-bom:33.3.0"))
    implementation("com.google.firebase:firebase-messaging")
}
