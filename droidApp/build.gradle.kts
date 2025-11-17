import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    id("io.sentry.android.gradle") version "5.12.1"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.appcompat)
            implementation(libs.material)
            implementation(libs.androidx.constraintlayout)
            implementation("io.insert-koin:koin-core:3.5.6")
            implementation("io.mockk:mockk:1.14.2")
            implementation("com.google.android.material:material:1.12.0")
            implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
            implementation("androidx.recyclerview:recyclerview:1.4.0")
            implementation("com.github.bumptech.glide:glide:4.16.0")
        }
        commonMain.dependencies {
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.movieskmp"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.example.movieskmp"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/native-image/io.sentry/sentry/native-image.properties"
            )
        }
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false   // ❌ Disable shrinking for debug builds
            isShrinkResources = false // ❌ Don’t remove unused resources
        }
        getByName("release") {
            isMinifyEnabled = true    // ✅ Enable shrinking, obfuscation, and optimization
            isShrinkResources = true  // ✅ Also shrink unused resources (optional)
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

// Prevent Sentry from being included in the Android app through the AGP.
//configurations {
//    compileOnly {
//        exclude(group = "io.sentry", module = "sentry")
//        exclude(group = "io.sentry", module = "sentry-android")
//    }
//}

sentry {
    // Auto-Install Sentry dependencies
    autoInstallation {
        enabled = false
    }

    //Read from the environment variable
    val token = System.getenv("SENTRY_AUTH_TOKEN") ?: ""
    //⚠️ NOTE you need to save it in the environment variable for example: in PowerSheell - setx SENTRY_AUTH_TOKEN "paste_your_token"
    if (token.isNotBlank())
    {
        // The slug of the Sentry organization to use for uploading proguard mappings/source contexts.
        org.set("freelance-6m")
        projectName.set("kotlin-bestapp")
        authToken.set(token)

        debug = false
        includeSourceContext = true
    }
    else
    {
        println("⚠️ SENTRY_AUTH_TOKEN is not set — skipping Sentry upload configuration.")
    }
}

