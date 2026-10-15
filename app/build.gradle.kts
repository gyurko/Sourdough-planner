plugins {
    id("com.android.application")
}

android {
    namespace = "com.sourdoughplanner.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sourdoughplanner.app"
        minSdk = 28
        targetSdk = 35
        versionCode = 3
        versionName = "1.2"
    }

    // Fixed key committed to the repo so every build (local or GitHub Actions) has the same
    // signature and updates install over the previous version. Fine for sideloading;
    // use a private key before distributing the app anywhere public.
    signingConfigs {
        create("sideload") {
            storeFile = file("sideload.keystore")
            storePassword = "sourdough"
            keyAlias = "sourdough"
            keyPassword = "sourdough"
        }
    }

    buildTypes {
        debug {
            signingConfig = signingConfigs.getByName("sideload")
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("sideload")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// No library dependencies: plain android.app.Activity + WebView + AlarmManager.
