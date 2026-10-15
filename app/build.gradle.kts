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
        versionCode = 2
        versionName = "1.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            // Signed with the debug key so the APK can be sideloaded directly.
            // Replace with your own signing config before publishing anywhere.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

// No library dependencies: plain android.app.Activity + WebView + AlarmManager.
