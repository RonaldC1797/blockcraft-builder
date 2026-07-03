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


        implementation("androidx.work:work-runtime-ktx:2.9.0")

        implementation(libs.androidx.lifecycle.viewmodel)
        implementation(libs.androidx.lifecycle.livedata)
    }