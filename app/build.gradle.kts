plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "dev.tsdroid.han"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yuaxi.ts6droid.cn"
        minSdk = 29
        targetSdk = 35
        versionCode = 8
        versionName = "2.2.1-Han"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (file("${rootDir}/release.keystore").exists()) {
            create("release") {
                storeFile = file("${rootDir}/release.keystore")
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "ts6droid"
                keyAlias = System.getenv("KEYSTORE_ALIAS") ?: "ts6droid"
                keyPassword = System.getenv("KEYSTORE_PASSWORD") ?: "ts6droid"
            }
        }
    }

    buildTypes {
        debug {
            if (signingConfigs.names.contains("release")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingConfigs.names.contains("release")) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    sourceSets {
        getByName("main") {
            java.srcDirs("src/main/java", "src/main/kotlin")
        }
    }
}

// Task to build Rust native libraries via cargo-ndk
tasks.register<Exec>("buildRustLibs") {
    workingDir = file("${rootDir}/../tslib_multi")
    val ndkDir = System.getenv("ANDROID_NDK_HOME")
        ?: System.getProperty("user.home")?.let { home ->
            val ndkBase = file("$home/Android/Sdk/ndk")
            if (ndkBase.exists()) {
                ndkBase.listFiles()?.maxByOrNull { it.name }?.absolutePath
            } else null
        }
    environment("ANDROID_NDK_HOME", ndkDir ?: "")
    environment("ANDROID_NDK", ndkDir ?: "")
    environment("CMAKE_POLICY_VERSION_MINIMUM", "3.5")
    commandLine(
        "cargo", "ndk",
        "-t", "arm64-v8a",
        "-t", "x86_64",
        "-t", "armeabi-v7a",
        "-o", "${projectDir}/src/main/jniLibs",
        "build", "--release", "-p", "tslib-jni",
        "--features", "vendored-openssl",
        "-j10"
    )
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    debugImplementation(libs.androidx.ui.tooling)
}
