pluginManagement {
    repositories {
        // ⚡ Gradle Plugin Portal (para org.jetbrains.kotlin.android y otros plugins)
        gradlePluginPortal()

        // 🔹 Repositorio principal de Google (Android SDK, Jetpack, Maps, Room, Firebase, etc.)
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // 🔹 Maven Central (Retrofit, OkHttp, Gson, Glide, etc.)
        mavenCentral()

        // ✅ Extra: soporte para bibliotecas alojadas en GitHub
        maven { url = uri("https://jitpack.io") }
    }

    // 🆕 Permite el plugin de Google Services para Firebase
    plugins {
        id("com.google.gms.google-services") version "4.4.2"
    }
}

dependencyResolutionManagement {
    // 🔒 Evita repositorios locales no controlados
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)

    repositories {
        // Repositorios principales (orden recomendado)
        google()           // Android SDK, Jetpack, Firebase
        mavenCentral()     // Retrofit, OkHttp, Gson, Glide, etc.
        maven { url = uri("https://jitpack.io") } // GitHub libs
    }
}

// 📦 Nombre del proyecto raíz
rootProject.name = "Inmobiliaria_2025"

// 🧩 Módulos incluidos en la compilación
include(":app")
