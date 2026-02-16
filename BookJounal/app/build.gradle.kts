plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)  // AÑADIDO
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "eus.arreseainhize.bookjounal"
    compileSdk = 36

    defaultConfig {
        applicationId = "eus.arreseainhize.bookjounal"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["appAuthRedirectScheme"] = "eus.arreseainhize.bookjounal"
        manifestPlaceholders["authRedirectScheme"] = "eus.arreseainhize.bookjounal"
    }

    buildTypes {
        debug {
            isDebuggable = true
        }
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("${System.getProperty("user.home")}${File.separator}.android${File.separator}debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.androidx.core.ktx)  // AÑADIDO
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.activity)

    // Navigation
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.navigation.fragment.ktx)  // AÑADIDO
    implementation(libs.navigation.ui.ktx)        // AÑADIDO

    // Firebase BOM
    implementation(platform(libs.firebase.bom))

    // Firebase Authentication
    implementation(libs.firebase.auth)
    implementation("com.google.firebase:firebase-auth")  // O usa tu referencia

    // Google Play Services Auth (para login con Google)
    implementation(libs.play.services.auth)  // AÑADIDO

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore")

    // Firebase Realtime Database (si lo usas)
    implementation("com.google.firebase:firebase-database")

    // Firebase UI (opcional)
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // Credential Manager (API oficial y moderna)
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Google Play Services Base
    implementation("com.google.android.gms:play-services-base:18.5.0")

    // Browser support (necesario para OAuth web)
    implementation("androidx.browser:browser:1.7.0")

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)

    // Glide
    implementation(libs.glide)
    annotationProcessor(libs.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}