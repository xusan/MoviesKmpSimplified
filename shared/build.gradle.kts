import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    id("io.realm.kotlin") version "3.0.0"
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedAppCore"
            isStatic = true
        }
    }

    sourceSets {
        val ktorVersion = "3.0.0"
        commonMain.dependencies {
            implementation(libs.kotlin.stdlib)
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            implementation("org.jetbrains.kotlinx:atomicfu:0.24.0")

            //DI container
            implementation("io.insert-koin:koin-core:3.5.6")
            //datetime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
            //local database
            implementation("io.realm.kotlin:library-base:3.0.0")
            implementation("com.squareup.okio:okio:3.9.0")
            //httpClient
            implementation("io.ktor:ktor-client-core:${ktorVersion}")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            implementation("com.benasher44:uuid:0.8.4")

        }
        androidMain {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:${ktorVersion}")
                implementation(libs.androidx.appcompat)
                implementation("com.github.tony19:logback-android:3.0.0")

                //browser
                implementation("androidx.browser:browser:1.8.0")

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        iosMain {
            dependencies{
                implementation("io.ktor:ktor-client-darwin:${ktorVersion}")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
            }
        }
        jvmMain.dependencies {
            implementation("io.ktor:ktor-client-cio:$ktorVersion")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.movieskmp.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    // Enable Android resources (layouts, drawables, etc.) in shared/src/androidMain/res
    // This allows the shared module to contain Android-specific UI resources
    sourceSets {
        named("main") {
            res.srcDirs("src/androidMain/res")
            manifest.srcFile("src/androidMain/AndroidManifest.xml")
        }
    }
}
