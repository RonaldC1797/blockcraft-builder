# 🧱 Blockcraft Builder

> **Construye sin límites. Sin conexión. Sin complicaciones.**

Una aplicación Android de construcción 3D con bloques diseñada para niños, adolescentes y adultos que buscan una experiencia creativa, relajante y completamente offline.

---

## 📋 Tabla de Contenidos

- [Descripción del Problema](#-descripción-del-problema)
- [Objetivo de la Aplicación](#-objetivo-de-la-aplicación)
- [Historias de Usuario — MVP](#-historias-de-usuario--mvp)
- [Tecnología Utilizada](#-tecnología-utilizada)
- [Instalación](#-instalación)
- [Capturas de Pantalla](#-capturas-de-pantalla)
- [Estado del Proyecto](#-estado-del-proyecto)

---

## 🚧 Descripción del Problema

Los juegos de construcción más populares del mercado móvil, como **Minecraft** o **Roblox**, presentan barreras que frustran la experiencia de muchos usuarios:

- **Complejidad elevada**: curvas de aprendizaje empinadas que desalientan a jugadores nuevos o jóvenes.
- **Dependencia de Internet**: requieren conexión constante para funcionar, lo que limita el acceso en zonas con conectividad limitada.
- **Microtransacciones agresivas**: interrumpen la experiencia con compras que condicionan el progreso o el contenido disponible.

**Blockcraft Builder** resuelve esto ofreciendo:

- ✅ Un entorno **sencillo e intuitivo**, accesible desde los primeros minutos.
- ✅ Funcionamiento **100% offline**, sin depender de servidores externos.
- ✅ Experiencia **libre de micropagos**, completa desde el primer uso.
- ✅ Orientado a **niños y adolescentes de 8 a 16 años**, así como a adultos que buscan una actividad creativa y relajante.

---

## 🎯 Objetivo de la Aplicación

Desarrollar una aplicación nativa de construcción 3D para Android que permita:

- Colocar, rotar y eliminar bloques en un **grid tridimensional de 16×16×16**.
- Operar de forma **completamente offline**, sin requerir conexión a Internet en ningún momento.
- Mantener una **latencia menor a 2 segundos** en dispositivos Android de gama media.
- Brindar una experiencia educativa y lúdica, accesible para usuarios de distintas edades y niveles de experiencia.

---

## 👤 Historias de Usuario — MVP

| ID | Rol | Necesidad | Beneficio |
|----|-----|-----------|-----------|
| **HU-01** | Jugador | Guardar mi construcción con un nombre y recuperarla desde la pantalla de inicio | Continuar sin perder el progreso |
| **HU-02** | Jugador joven | Deshacer la colocación del último bloque con un botón visible | Corregir errores sin reiniciar toda la construcción |
| **HU-03** | Adulto que personaliza su construcción | Navegar los bloques organizados por categoría | Encontrar rápidamente el tipo de bloque que necesito |
| **HU-04** | Usuario que terminó una construcción | Exportar una captura y compartirla mediante las apps instaladas en el dispositivo | Mostrar y guardar mi trabajo fácilmente |
| **HU-05** | Jugador que busca precisión | Activar o desactivar una cuadrícula visual de referencia | Colocar bloques de forma más ordenada y precisa |

---

## 🛠️ Tecnología Utilizada

| Capa | Tecnología |
|------|------------|
| **Lenguaje** | Kotlin |
| **IDE** | Android Studio |
| **Arquitectura** | MVVM (Model-View-ViewModel) |
| **Base de datos** | Room Database |
| **Gestión de estado** | ViewModel + LiveData |
| **Diseño de interfaz** | Material Design 3 |
| **Renderizado 3D** | SurfaceView / OpenGL ES |
| **Control de versiones** | Git + GitHub |

### Estructura del Proyecto

```
blockcraft-builder/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/blockcraft/
│   │   │   │   ├── data/          # Room Database, DAOs, entidades
│   │   │   │   ├── model/         # Modelos de datos
│   │   │   │   ├── ui/            # Activities, Fragments, Adapters
│   │   │   │   ├── viewmodel/     # ViewModels por pantalla
│   │   │   │   └── renderer/      # Lógica de renderizado 3D
│   │   │   └── res/               # Layouts, drawables, strings
│   └── build.gradle
├── README.md
└── build.gradle
```

---

## ⚙️ Instalación

### Requisitos Previos

- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK 17** o superior
- **Android SDK** — API Level 26 (Android 8.0) como mínimo
- **Git** instalado en el sistema

### Pasos

1. **Clonar el repositorio**

   ```bash
   git clone https://github.com/tu-usuario/blockcraft-builder.git
   cd blockcraft-builder
   ```

2. **Abrir en Android Studio**

   - Inicia Android Studio.
   - Selecciona **File → Open** y elige la carpeta del proyecto.
   - Espera a que Gradle sincronice las dependencias automáticamente.

3. **Ejecutar la aplicación**

   - Conecta un dispositivo Android físico (recomendado) o configura un emulador AVD con API 26+.
   - Presiona **Run → Run 'app'** o usa el atajo `Shift + F10`.

4. **Permisos requeridos**

   La aplicación solicita los siguientes permisos en tiempo de ejecución:

   ```xml
   <!-- Necesario para exportar y compartir capturas de pantalla -->
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   ```

> **Nota:** No se requiere conexión a Internet en ningún momento para usar la aplicación.

---

## 📸 Capturas de Pantalla

> 🚧 *Las capturas de pantalla serán añadidas una vez se complete la implementación en Android Studio.*

| Pantalla de Login | Editor de Construcción |
|:-----------------:|:---------------------:|
| `[<img width="406" height="874" alt="Login" src="https://github.com/user-attachments/assets/6fdb49c6-4f80-4e58-9b16-9e6c6cf26f16" />]` | `[Placeholder]` |

| *Listado de mundos guardados* | *Grid 3D con controles de bloque* |

| Selector de Bloques | Menú de Opciones |
|:------------------:|:----------------:|
| `[Placeholder]` | `[Placeholder]` |
| *Catálogo organizado por categorías* | *Exportar, guardar y ajustes* |

---

## 📊 Estado del Proyecto

```
[██████████░░░░░░░░░░]  45% completado
```

| Fase | Estado |
|------|--------|
| ✅ Prototipo de alta fidelidad en Figma | Completado |
| ✅ Entorno Android Studio configurado | Completado |
| ✅ Repositorio GitHub inicializado | Completado |
| 🔄 Implementación de pantallas en Android Studio | En progreso |
| ⏳ Integración de Room Database | Pendiente |
| ⏳ Renderizado del grid 3D | Pendiente |
| ⏳ Pruebas en dispositivos físicos | Pendiente |


## ✅ Funcionalidades implementadas

### Firebase Authentication

- **Registro de usuarios** con validación en 14 pasos (nombre, email, contraseña con mayúscula, número y minúscula, confirmación)
- **Inicio de sesión** con email y contraseña mediante Firebase Authentication
- **Manejo de errores** de Firebase en español (correo ya registrado, contraseña incorrecta, usuario no encontrado)
- **Persistencia de sesión** — si el usuario ya inició sesión, la app lo lleva directo a MainActivity sin pasar por Login
- **Cierre de sesión** con redirección automática al Login
- **Protección del Back Stack** con `FLAG_ACTIVITY_CLEAR_TASK` — el botón Atrás no regresa al Login tras autenticarse
- **Perfil de usuario** — nombre y email del usuario autenticado visibles en MainActivity
- **Pantalla principal** con AppBar, TabLayout (Todos / Naturaleza / Construcción) y BottomNavigationView (Inicio / Mundos / Inventario / Perfil)
- **Tarjetas de progreso** — Bloques colocados y Horas de juego
