plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.lightspeed.browser"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.lightspeed.browser"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        // Keep method count under 64K → no multidex needed
        // This is critical for low-end device startup performance

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable explicit API for better code generation
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-Xjvm-default=all"  // Generate default methods in interfaces
        )
    }

    buildFeatures {
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Keep APK lean
            excludes += "/META-INF/*.kotlin_module"
            excludes += "/META-INF/*.version"
        }
    }

    // Only target ARM devices — x86/x86_64 are not used in low-end phones
    splits {
        abi {
            isEnable = true
            reset()
            include("armeabi-v7a", "arm64-v8a")
            isUniversalApk = true
        }
    }
}

dependencies {
    // ── Kotlin ──────────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // ── AndroidX (minimal) ───────────────────────────────────
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Lifecycle (lightweight — only what we need)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-process:2.7.0")

    // ── Database ────────────────────────────────────────────
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // ── Debugging (debug only) ──────────────────────────────
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.13")

    // ── Testing ─────────────────────────────────────────────
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // NOTE: Deliberately excluded
    // - Hilt/Dagger (manual DI instead — saves 5MB APK, ~100ms startup)
    // - Jetpack Compose (Custom Views instead — saves 3MB APK)
    // - Coil/Glide (manual BitmapFactory — saves 1.6MB)
    // - Retrofit (OkHttp directly — simpler, lighter)
    // - Navigation Component (Fragment transactions — lighter)
    // - Lottie (no animations — faster rendering)
    // - Material Design extras (AppCompat only — smaller)
    // - DataStore (SharedPrefs — lighter for simple settings)
}
