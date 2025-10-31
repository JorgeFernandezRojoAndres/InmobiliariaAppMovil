# File Tree: Inmobiliaria_2025

**Generated:** 10/17/2025, 9:42:57 PM
**Root Path:** `f:\Users\jf_wo\AndroidStudioProjects\Inmobiliaria_2025`

```
├── 📁 app
│   ├── 📁 src
│   │   ├── 📁 androidTest
│   │   │   └── 📁 java
│   │   │       └── 📁 com
│   │   │           └── 📁 jorge
│   │   │               └── 📁 inmobiliaria2025
│   │   │                   └── ☕ ExampleInstrumentedTest.java
│   │   ├── 📁 main
│   │   │   ├── 📁 java
│   │   │   │   └── 📁 com
│   │   │   │       └── 📁 jorge
│   │   │   │           └── 📁 inmobiliaria2025
│   │   │   │               ├── 📁 adapter
│   │   │   │               │   └── ☕ InmueblesAdapter.java
│   │   │   │               ├── 📁 data
│   │   │   │               │   ├── 📁 network
│   │   │   │               │   │   ├── ☕ Api.java
│   │   │   │               │   │   └── ☕ RetrofitClient.java
│   │   │   │               │   ├── ☕ InmobiliariaDatabase.java
│   │   │   │               │   ├── ☕ InmuebleDao.java
│   │   │   │               │   ├── ☕ InmuebleRepository.java
│   │   │   │               │   └── ☕ SessionManager.java
│   │   │   │               ├── 📁 model
│   │   │   │               │   ├── ☕ CambioClaveDto.java
│   │   │   │               │   ├── ☕ Contrato.java
│   │   │   │               │   ├── ☕ Inmueble.java
│   │   │   │               │   ├── ☕ Inquilino.java
│   │   │   │               │   ├── ☕ LoginRequest.java
│   │   │   │               │   ├── ☕ Pago.java
│   │   │   │               │   ├── ☕ Propietario.java
│   │   │   │               │   ├── ☕ TipoInmueble.java
│   │   │   │               │   └── ☕ TokenResponse.java
│   │   │   │               ├── 📁 utils
│   │   │   │               │   └── ☕ FileUtils.java
│   │   │   │               ├── 📁 view
│   │   │   │               │   ├── ☕ DetalleInmuebleFragment.java
│   │   │   │               │   ├── ☕ InmueblesFragment.java
│   │   │   │               │   ├── ☕ LoginActivity.java
│   │   │   │               │   ├── ☕ LogoutFragment.java
│   │   │   │               │   ├── ☕ NuevoInmuebleFragment.java
│   │   │   │               │   ├── ☕ PerfilFragment.java
│   │   │   │               │   ├── ☕ PlaceholderFragment.java
│   │   │   │               │   ├── ☕ UbicacionFragment.java
│   │   │   │               │   └── 📝 Untitled-1.md
│   │   │   │               ├── 📁 viewmodel
│   │   │   │               │   ├── ☕ InmuebleViewModel.java
│   │   │   │               │   ├── ☕ PerfilViewModel.java
│   │   │   │               │   └── ☕ UbicacionViewModel.java
│   │   │   │               ├── ☕ InmobiliariaApp.java
│   │   │   │               └── ☕ MainActivity.java
│   │   │   ├── 📁 res
│   │   │   │   ├── 📁 drawable
│   │   │   │   │   ├── ⚙️ avatar_border.xml
│   │   │   │   │   ├── ⚙️ circle_background.xml
│   │   │   │   │   ├── ⚙️ ic_baseline_add_24.xml
│   │   │   │   │   ├── ⚙️ ic_baseline_edit_24.xml
│   │   │   │   │   ├── ⚙️ ic_baseline_photo_camera_24.xml
│   │   │   │   │   ├── ⚙️ ic_baseline_save_24.xml
│   │   │   │   │   ├── ⚙️ ic_image_placeholder.xml
│   │   │   │   │   ├── ⚙️ ic_launcher_background.xml
│   │   │   │   │   ├── ⚙️ ic_launcher_foreground.xml
│   │   │   │   │   ├── ⚙️ ic_person.xml
│   │   │   │   │   ├── ⚙️ image_background.xml
│   │   │   │   │   └── ⚙️ image_border.xml
│   │   │   │   ├── 📁 layout
│   │   │   │   │   ├── ⚙️ activity_login.xml
│   │   │   │   │   ├── ⚙️ activity_main.xml
│   │   │   │   │   ├── ⚙️ dialog_cambiar_clave.xml
│   │   │   │   │   ├── ⚙️ fragment_contratos.xml
│   │   │   │   │   ├── ⚙️ fragment_detalle_inmueble.xml
│   │   │   │   │   ├── ⚙️ fragment_inmuebles.xml
│   │   │   │   │   ├── ⚙️ fragment_logout.xml
│   │   │   │   │   ├── ⚙️ fragment_nuevo_inmueble.xml
│   │   │   │   │   ├── ⚙️ fragment_pagos.xml
│   │   │   │   │   ├── ⚙️ fragment_perfil.xml
│   │   │   │   │   ├── ⚙️ fragment_placeholder.xml
│   │   │   │   │   ├── ⚙️ fragment_ubicacion.xml
│   │   │   │   │   ├── ⚙️ item_inmueble.xml
│   │   │   │   │   └── ⚙️ nav_header.xml
│   │   │   │   ├── 📁 menu
│   │   │   │   │   └── ⚙️ drawer_menu.xml
│   │   │   │   ├── 📁 mipmap-anydpi-v26
│   │   │   │   │   ├── ⚙️ ic_launcher.xml
│   │   │   │   │   └── ⚙️ ic_launcher_round.xml
│   │   │   │   ├── 📁 mipmap-hdpi
│   │   │   │   │   ├── 🖼️ ic_launcher.webp
│   │   │   │   │   └── 🖼️ ic_launcher_round.webp
│   │   │   │   ├── 📁 mipmap-mdpi
│   │   │   │   │   ├── 🖼️ ic_launcher.webp
│   │   │   │   │   └── 🖼️ ic_launcher_round.webp
│   │   │   │   ├── 📁 mipmap-xhdpi
│   │   │   │   │   ├── 🖼️ ic_launcher.webp
│   │   │   │   │   └── 🖼️ ic_launcher_round.webp
│   │   │   │   ├── 📁 mipmap-xxhdpi
│   │   │   │   │   ├── 🖼️ ic_launcher.webp
│   │   │   │   │   └── 🖼️ ic_launcher_round.webp
│   │   │   │   ├── 📁 mipmap-xxxhdpi
│   │   │   │   │   ├── 🖼️ ic_launcher.webp
│   │   │   │   │   └── 🖼️ ic_launcher_round.webp
│   │   │   │   ├── 📁 navigation
│   │   │   │   │   └── ⚙️ nav_graph.xml
│   │   │   │   ├── 📁 values
│   │   │   │   │   ├── ⚙️ colors.xml
│   │   │   │   │   ├── ⚙️ strings.xml
│   │   │   │   │   └── ⚙️ themes.xml
│   │   │   │   ├── 📁 values-night
│   │   │   │   │   └── ⚙️ themes.xml
│   │   │   │   └── 📁 xml
│   │   │   │       ├── ⚙️ backup_rules.xml
│   │   │   │       ├── ⚙️ data_extraction_rules.xml
│   │   │   │       └── ⚙️ network_security_config.xml
│   │   │   └── ⚙️ AndroidManifest.xml
│   │   └── 📁 test
│   │       └── 📁 java
│   │           └── 📁 com
│   │               └── 📁 jorge
│   │                   └── 📁 inmobiliaria2025
│   │                       └── ☕ ExampleUnitTest.java
│   ├── ⚙️ .gitignore
│   └── 📄 proguard-rules.pro
├── 📁 gradle
│   ├── 📁 wrapper
│   │   ├── 📄 gradle-wrapper.jar
│   │   └── 📄 gradle-wrapper.properties
│   └── ⚙️ libs.versions.toml
├── ⚙️ .gitignore
├── 📄 gradle.properties
├── 📄 gradlew
├── 📄 gradlew.bat
└── 📄 settings.gradle.kts
```

---
*Generated by FileTree Pro Extension*