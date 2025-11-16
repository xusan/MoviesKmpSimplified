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

sentry {
    // Auto-Install Sentry dependencies
    autoInstallation {
        enabled = true
    }
    // The slug of the Sentry organization to use for uploading proguard mappings/source contexts.
    org.set("freelance-6m")
    // The slug of the Sentry project to use for uploading proguard mappings/source contexts.
    projectName.set("kotlin-bestapp")
    // The authentication token to use for uploading proguard mappings/source contexts.
    // TODO: Do not expose this token in your build.gradle files, but rather set an environment variable and read it into this property.
    authToken.set("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")
    debug = false
    includeSourceContext = true
}

