# 📘 Technical Manual — Blockcraft Builder

**Version:** 1.0  
**Author:** Ronald Alexander Cacuango Cedillo  
**Institution:** Universidad Central del Ecuador  
**Last updated:** July 2026  

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technical Stack](#2-technical-stack)
3. [Architecture](#3-architecture)
4. [Data Model](#4-data-model)
5. [Module Description](#5-module-description)
6. [3D Engine — IsometricView](#6-3d-engine--isometricview)
7. [Firebase Integration](#7-firebase-integration)
8. [Notifications — WorkManager](#8-notifications--workmanager)
9. [Testing](#9-testing)
10. [Build Instructions](#10-build-instructions)
11. [Project Structure](#11-project-structure)
12. [Performance Metrics](#12-performance-metrics)
13. [Known Issues & Roadmap](#13-known-issues--roadmap)

---

## 1. Project Overview

**Blockcraft Builder** is a native Android application for 3D isometric block construction, designed for users aged 8–16. It operates **100% offline** using Room Database for local persistence and Firebase Authentication for user identity.

| Property | Value |
|---|---|
| Package name | `com.cacuango.blockcraft_builder` |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Language | Kotlin |
| Architecture | MVVM + Repository Pattern |
| Database | Room (SQLite) |
| Authentication | Firebase Auth |

---

## 2. Technical Stack

### Core Libraries

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.1.0 | Primary language |
| Room Runtime | 2.6.1 | Local SQLite persistence |
| Room KTX | 2.6.1 | Coroutine extensions for Room |
| KSP | 2.1.0-1.0.29 | Annotation processor for Room |
| Firebase Auth | 23.2.0 | User authentication |
| WorkManager | 2.9.0 | Background task scheduling |
| Lifecycle ViewModel | 2.8.7 | MVVM ViewModel |
| Lifecycle LiveData | 2.8.7 | Reactive UI updates |
| Material Design | 1.14.0 | UI components |
| ConstraintLayout | 2.2.1 | Layout management |
| AppCompat | 1.7.1 | Backward compatibility |

### Testing Libraries

| Library | Version | Purpose |
|---|---|---|
| JUnit 4 | 4.13.2 | Unit testing |
| AndroidX Test Core | 1.6.1 | Android testing utilities |
| Room Testing | 2.6.1 | In-memory database testing |
| Espresso Core | 3.5.1 | UI testing |
| Coroutines Test | 1.7.3 | Coroutine testing |

### Assets

| Asset | License | Purpose |
|---|---|---|
| Kenney Isometric Blocks | CC0 (Public Domain) | Block textures for 3D editor |

---

## 3. Architecture

Blockcraft Builder follows the **MVVM (Model-View-ViewModel)** pattern with a strict Repository layer separating data access from business logic.

```
┌─────────────────────────────────────┐
│           UI Layer                  │
│  Activities · RecyclerView          │
│  IsometricView (Custom Canvas)      │
└──────────────┬──────────────────────┘
               │ observes LiveData
┌──────────────▼──────────────────────┐
│         ViewModel Layer             │
│  ProyectoViewModel                  │
│  viewModelScope · Dispatchers.IO    │
└──────────────┬──────────────────────┘
               │ delegates
┌──────────────▼──────────────────────┐
│        Repository Layer             │
│  ProyectoRepository                 │
│  Single source of truth             │
└──────────────┬──────────────────────┘
               │ queries
┌──────────────▼──────────────────────┐
│           DAO Layer                 │
│  ProyectoDao · BloqueDao            │
│  TipoBloqueDao · HistorialAccionDao │
└──────────────┬──────────────────────┘
               │ persists
┌──────────────▼──────────────────────┐
│        Room Database                │
│  blockcraft_database (SQLite)       │
│  Version: 3                         │
└─────────────────────────────────────┘
```

**Key principle:** Each layer only communicates with the immediately adjacent layer. The UI never accesses the DAO directly.

---

## 4. Data Model

### Entity: Proyecto

```kotlin
@Entity(tableName = "proyectos")
data class Proyecto(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val fechaCreacion: String,
    val fechaModificacion: String,
    val camaraX: Float,
    val camaraY: Float,
    val camaraZ: Float,
    val categoria: String = "Todos"
)
```

### Entity: Bloque

```kotlin
@Entity(tableName = "bloques")
data class Bloque(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val proyectoId: Int,
    val tipoId: String,
    val posX: Int,
    val posY: Int,
    val posZ: Int,
    val ordenColocacion: Int = 0
)
```

### Entity: TipoBloque

```kotlin
@Entity(tableName = "TipoBloque")
data class TipoBloque(
    @PrimaryKey val id: String,
    val nombre_display: String,
    val activo: Int = 1,
    val orden_paleta: Int
)
```

### Entity: HistorialAccion

```kotlin
@Entity(
    tableName = "HistorialAccion",
    foreignKeys = [ForeignKey(
        entity = Proyecto::class,
        parentColumns = ["id"],
        childColumns = ["id_proyecto"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class HistorialAccion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val id_proyecto: Int,
    val tipo_accion: String,   // "COLOCAR" or "DESHACER"
    val tipo_bloque: String,
    val pos_x: Int,
    val pos_y: Int,
    val pos_z: Int,
    val orden: Int,            // max 20 actions per project
    val fecha_hora: String
)
```

### Database Schema

```
proyectos ──────────────────────────────────────┐
│ id (PK)                                        │
│ nombre                                         │
│ fechaCreacion                                  │
│ fechaModificacion                              │
│ categoria                                      │
└────────────┬───────────────────────────────────┘
             │ 1:N                    │ 1:N
      ┌──────▼──────┐        ┌───────▼────────┐
      │   bloques   │        │ HistorialAccion │
      │ id (PK)     │        │ id (PK)         │
      │ proyectoId  │        │ id_proyecto     │
      │ tipoId      │        │ tipo_accion     │
      │ posX/Y/Z    │        │ tipo_bloque     │
      └─────────────┘        │ pos_x/y/z       │
                             │ orden           │
                             └─────────────────┘

TipoBloque (standalone)
│ id (PK)
│ nombre_display
│ activo
│ orden_paleta
```

---

## 5. Module Description

### UI Layer

| Class | Package | Description |
|---|---|---|
| `LoginActivity` | `ui.auth` | Firebase email/password login |
| `RegisterActivity` | `ui.auth` | New user registration |
| `MainActivity` | `ui.home` | Home screen with recent projects |
| `CreateProjectActivity` | `ui.create` | Create and edit projects |
| `EditorActivity` | `ui.editor` | 3D isometric block editor |
| `IsometricView` | `ui.editor` | Custom Canvas 3D engine |
| `LoadWorldActivity` | `ui.load` | Project list with search and filters |
| `MundoAdapter` | `ui.load` | RecyclerView adapter for projects |
| `InventarioBottomSheet` | `ui.home` | Block inventory bottom sheet |

### Data Layer

| Class | Package | Description |
|---|---|---|
| `AppDatabase` | `data.local.database` | Room singleton database |
| `ProyectoDao` | `data.local.dao` | CRUD operations for Proyecto |
| `BloqueDao` | `data.local.dao` | CRUD operations for Bloque |
| `TipoBloqueDao` | `data.local.dao` | Read operations for TipoBloque |
| `HistorialAccionDao` | `data.local.dao` | Action history operations |
| `ProyectoRepository` | `data.repository` | Single source of truth |

### Application Layer

| Class | Package | Description |
|---|---|---|
| `BlockcraftApp` | root | Application class, notification channels |
| `ProyectoViewModel` | `viewmodel` | Business logic, LiveData, coroutines |
| `RecordatorioWorker` | `workers` | WorkManager background notification |

---

## 6. 3D Engine — IsometricView

`IsometricView` is a custom Android `View` that implements a full isometric 3D engine using the `Canvas` API.

### Key Properties

```kotlin
private val GRID_SIZE = 20          // 20x20 grid
private val MAX_HEIGHT = 10         // max block stack height
private val BLOCK_WIDTH = 80f
private val BLOCK_HEIGHT = 40f
private val BLOCK_DEPTH = 24f
```

### Coordinate System

The engine converts grid coordinates to screen coordinates using isometric projection with rotation support:

```kotlin
private val screenResult = FloatArray(2)  // reused to avoid allocations

private fun toScreen(col: Int, fila: Int, altura: Int): FloatArray {
    val cx = col - GRID_SIZE / 2f
    val cz = fila - GRID_SIZE / 2f
    val rx = cx * cos(rotacion) - cz * sin(rotacion)
    val rz = cx * sin(rotacion) + cz * cos(rotacion)
    val bw = BLOCK_WIDTH * zoom
    val bh = BLOCK_HEIGHT * zoom
    val bd = BLOCK_DEPTH * zoom
    screenResult[0] = cameraX + rx * (bw/2) - rz * (bw/2) - bw/2
    screenResult[1] = cameraY + rx * (bh/2) + rz * (bh/2) - altura * bd - bh/2
    return screenResult
}
```

### Touch Gestures

| Gesture | Action |
|---|---|
| Single tap | Place or remove block at level |
| One finger drag | Move camera |
| Two finger rotate | Rotate scene |
| Two finger pinch | Zoom in/out (0.3x – 3.0x) |
| + / - buttons | Change block placement level (0–9) |

### Block Categories

| Category | Block IDs | Count |
|---|---|---|
| Estructura | `blk_estructura_*` | 11 blocks |
| Decoración | `blk_deco_*` | 4 blocks |
| Naturaleza | `blk_nat_*` | 14 blocks |
| Especiales | `blk_esp_*` | 11 blocks |

---

## 7. Firebase Integration

Blockcraft Builder uses **Firebase Authentication** exclusively for user identity. Room Database is not synchronized with any cloud service — all project data remains local.

### Authentication Flow

```
User opens app
      ↓
LoginActivity checks FirebaseAuth.currentUser
      ↓
currentUser != null → MainActivity
currentUser == null → show Login form
      ↓
user submits credentials
      ↓
FirebaseAuth.signInWithEmailAndPassword()
      ↓
success → MainActivity
failure → show error message
```

### Configuration

Requires `google-services.json` in `app/` directory. Obtain from Firebase Console → Project Settings → Your apps.

---

## 8. Notifications — WorkManager

### Channel Configuration

Defined in `BlockcraftApp.onCreate()`:

| Channel ID | Importance | Description |
|---|---|---|
| `canal_recordatorios` | DEFAULT | Project reminder after 3 days inactive |
| `canal_guardado` | LOW | Auto-save confirmation |
| `canal_logros` | HIGH | Block placement milestone |
| `canal_exportacion` | DEFAULT | Screenshot export complete |

### RecordatorioWorker

Runs every 3 days using `PeriodicWorkRequestBuilder`:

```kotlin
val solicitud = PeriodicWorkRequestBuilder<RecordatorioWorker>(3, TimeUnit.DAYS).build()
WorkManager.getInstance(this).enqueueUniquePeriodicWork(
    RecordatorioWorker.WORK_NAME,
    ExistingPeriodicWorkPolicy.KEEP,
    solicitud
)
```

---

## 9. Testing

### Unit Tests — `src/test/`

**Class:** `ValidacionesTest`  
**Framework:** JUnit 4  
**Device:** JVM (no emulator required)

| Test | Function tested | Result |
|---|---|---|
| `validarNombre_vacio` | `validarNombreProyecto("")` | ✅ PASS |
| `validarNombre_corto` | `validarNombreProyecto("AB")` | ✅ PASS |
| `validarNombre_especiales` | `validarNombreProyecto("A#1")` | ✅ PASS |
| `validarNombre_valido` | `validarNombreProyecto("Castillo")` | ✅ PASS |
| `validarNombre_soloEspacios` | `validarNombreProyecto("   ")` | ✅ PASS |
| `validarNombre_tresChars` | `validarNombreProyecto("ABC")` | ✅ PASS |
| `validarNombre_soloNumeros` | `validarNombreProyecto("123")` | ✅ PASS |
| `validarNombre_largo` | `validarNombreProyecto("A"*100)` | ✅ PASS |
| `formatearFecha_valido` | `formatearFecha("1751414400000")` | ✅ PASS |
| `formatearFecha_cero` | `formatearFecha("0")` | ✅ PASS |
| `formatearFecha_negativo` | `formatearFecha("-86400000")` | ✅ PASS |
| `formatearFecha_invalido` | `formatearFecha("texto")` | ✅ PASS |
| `formatearFecha_vacio` | `formatearFecha("")` | ✅ PASS |

**Total: 13 tests — 0 failures**

### Integration Tests — `src/androidTest/`

**Class:** `ProyectoDaoTest`  
**Framework:** JUnit 4 + Room in-memory database  
**Device:** Pixel 9 Pro emulator (API 17)

| Test | Operation | Result |
|---|---|---|
| `insertarProyecto_debeRetornarIdValido` | CREATE | ✅ PASS |
| `insertarYRecuperar_proyectoGuardado` | CREATE + READ | ✅ PASS |
| `obtenerTodos_baseDatosVacia` | READ empty | ✅ PASS |
| `obtenerTodos_dosProyectosInsertados` | READ multiple | ✅ PASS |
| `actualizarProyecto_nombreCambiado` | UPDATE | ✅ PASS |
| `eliminarProyecto_noDebeExistir` | DELETE | ✅ PASS |
| `eliminarProyecto_bloqueAsociado` | DELETE cascade | ✅ PASS |
| `buscarPorNombre_queryParcial` | SEARCH | ✅ PASS |
| `buscarPorNombre_sinCoincidencias` | SEARCH empty | ✅ PASS |

**Total: 9 tests — 0 failures**

### UI Tests — `src/androidTest/`

**Class:** `LoginActivityTest`  
**Framework:** Espresso  
**Device:** HONOR CRT-NX3 (physical device, API 35)

| Test | Action | Result |
|---|---|---|
| `loginActivity_alAbrir_debeMostrarCampos` | Open activity | ✅ PASS |
| `loginActivity_escribirEmail` | Type email | ✅ PASS |
| `loginActivity_camposVacios_botonVisible` | Empty submit | ✅ PASS |
| `loginActivity_enlaceRegistro_visible` | Register link | ✅ PASS |

**Total: 4 tests — 0 failures**

---

## 10. Build Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK API 34+
- Git

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/RonaldC1797/blockcraft-builder.git
cd blockcraft-builder

# 2. Open in Android Studio
# File → Open → select the cloned folder

# 3. Add Firebase configuration
# Copy google-services.json to app/ directory

# 4. Sync Gradle
# Click "Sync Now" when prompted, or:
# File → Sync Project with Gradle Files

# 5. Run the app
# Select emulator API 33+ or physical device
# Click Run ▶ or press Shift+F10
```

### Generate Debug APK

```
Build → Build Bundle(s) / APK(s) → Build APK(s)
Output: app/build/outputs/apk/debug/app-debug.apk
```

### Generate Release APK

```
Build → Generate Signed Bundle / APK → APK
Keystore: blockcraft_keystore.jks
Output: app/release/app-release.apk
```

### Run Tests

```bash
# Unit tests (no emulator)
./gradlew testDebugUnitTest

# Integration + UI tests (requires emulator or device)
./gradlew connectedDebugAndroidTest
```

---

## 11. Project Structure

```
BlockCraft_Builder/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/cacuango/blockcraft_builder/
│   │   │   │   ├── BlockcraftApp.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   ├── ProyectoDao.kt
│   │   │   │   │   │   │   ├── BloqueDao.kt
│   │   │   │   │   │   │   ├── TipoBloqueDao.kt
│   │   │   │   │   │   │   └── HistorialAccionDao.kt
│   │   │   │   │   │   ├── database/
│   │   │   │   │   │   │   └── AppDatabase.kt
│   │   │   │   │   │   └── Entity/
│   │   │   │   │   │       └── Entity.kt
│   │   │   │   │   └── repository/
│   │   │   │   │       └── ProyectoRepository.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── LoginActivity.kt
│   │   │   │   │   │   └── RegisterActivity.kt
│   │   │   │   │   ├── create/
│   │   │   │   │   │   └── CreateProjectActivity.kt
│   │   │   │   │   ├── editor/
│   │   │   │   │   │   ├── EditorActivity.kt
│   │   │   │   │   │   └── IsometricView.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── MainActivity.kt
│   │   │   │   │   │   └── InventarioBottomSheet.kt
│   │   │   │   │   └── load/
│   │   │   │   │       ├── LoadWorldActivity.kt
│   │   │   │   │       └── MundoAdapter.kt
│   │   │   │   ├── viewmodel/
│   │   │   │   │   └── ProyectoViewModel.kt
│   │   │   │   └── workers/
│   │   │   │       └── RecordatorioWorker.kt
│   │   │   ├── res/
│   │   │   │   ├── drawable/         # block textures + UI assets
│   │   │   │   ├── layout/           # XML layouts
│   │   │   │   └── values/           # colors, strings, themes
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                     # unit tests
│   │   └── androidTest/              # integration + UI tests
│   └── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
└── README.md
```

---

## 12. Performance Metrics

Measured with Android Profiler on Pixel 9 Pro emulator (API 37):

| Metric | Value | Reference | Status |
|---|---|---|---|
| Cold start time | 2,679 ms | < 5,000 ms | ✅ Normal |
| Warm start time | 1,600 ms | < 2,000 ms | ✅ Good |
| Memory usage (editor) | 181 MB | < 300 MB (3D apps) | ✅ Normal |
| Room query response | < 50 ms | < 200 ms | ✅ Excellent |
| API response time | N/A | N/A | 100% offline |

### Optimization Applied

**`IsometricView.toScreen()` — FloatArray reuse**

Replaced `Pair<Float, Float>` return type with a reusable `FloatArray(2)` instance to eliminate 36,374 object allocations per editor session, reducing GC pressure during 3D rendering.

---

## 13. Known Issues & Roadmap

### v1.0 — Known Issues

| # | Issue | Severity | Status |
|---|---|---|---|
| 1 | Low text contrast on cream background | Low | Pending v1.1 |

### v1.1 — Planned Features

- **HU-04:** Export and share construction screenshot via Android Intent
- Multi-user support — filter projects by Firebase UID
- Splash Screen API for faster perceived startup
- Text contrast improvements throughout the app
- Undo history persistence across sessions

---

## License

This project was developed for academic purposes at Universidad Central del Ecuador.  
Block textures from [Kenney Isometric Blocks](https://kenney.nl) — licensed under CC0 (Public Domain).

---

*Blockcraft Builder v1.0 — Ronald Alexander Cacuango Cedillo — UCE 2026*
