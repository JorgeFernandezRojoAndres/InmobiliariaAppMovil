pluginManagement {
    repositories {
        // ⚡ Gradle Plugin Portal (para org.jetbrains.kotlin.android y otros plugins)
        gradlePluginPortal()

        // 🔹 Repositorio principal de Google (Android SDK, Jetpack, Maps, Room, etc.)
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }

        // 🔹 Maven Central (Retrofit, OkHttp, Gson, Glide, etc.)
        mavenCentral()

        // ✅ Extra: soporte para bibliotecas alojadas en GitHub (por ejemplo, Glide Snapshot, Lottie, etc.)
        maven { url = uri("https://jitpack.io") }
    }
}

dependencyResolutionManagement {
    // 🔒 Evita repositorios locales no controlados
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // Repositorios principales (orden recomendado)
        google()           // Android SDK y librerías de Jetpack
        mavenCentral()     // Retrofit, OkHttp, Gson, Glide, etc.

        // ✅ Repositorio adicional opcional para proyectos alojados en GitHub
        maven { url = uri("https://jitpack.io") }
    }
}

// 📦 Nombre del proyecto raíz
rootProject.name = "Inmobiliaria_2025"

// 🧩 Módulos incluidos en la compilación
include(":app")
