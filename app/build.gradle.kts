plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}
    android {
        namespace = "com.cacuango.blockcraft.builder"
        compileSdk = 34

        defaultConfig {
            applicationId = "com.cacuango.blockcraft.builder"
            minSdk = 24
            targetSdk = 34
            versionCode = 1
            versionName = "1.0"

            testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            testInstrumentationRunnerArguments["clearPackageData"] = "true"
        }
        testOptions {
            execution = "ANDROIDX_TEST_ORCHESTRATOR"
            animationsDisabled = true  // ← importante para Espresso en API 33+
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
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }

        // ✅ CORRECTO - así se escribe
        kotlinOptions {
            jvmTarget = "17"   // ✅ No "1/", no "1.8", es "17"
        }


        // ✅ AGREGAR ESTO - HABILITAR VIEWBINDING
        buildFeatures {
            viewBinding = true
        }
    }

    dependencies {
        // Core
        implementation("androidx.core:core-ktx:1.12.0")
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.activity:activity:1.8.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")


        // Firebase
        implementation(platform("com.google.firebase:firebase-bom:33.12.0"))
        implementation("com.google.firebase:firebase-auth")

        // Room
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        ksp("androidx.room:room-compiler:2.6.1")

        // Testing
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

        // ── Pruebas de integración con Room in-memory ───────
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test:core:1.6.1")
        androidTestImplementation("androidx.room:room-testing:2.6.1")
        androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
        androidTestImplementation("androidx.test:runner:1.6.1")
        androidTestImplementation("androidx.test:rules:1.6.1")

        androidTestImplementation("androidx.test:runner:1.6.1")
        androidTestUtil("androidx.test:orchestrator:1.5.0")

        implementation("androidx.work:work-runtime-ktx:2.9.0")



        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.livedata)
    }