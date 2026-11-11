plugins {
    id("com.android.application") version "8.7.3" apply false
}

// Android Studio Application Configuration
// When opened in Android Studio, this module bundles the web app from app/src/main/assets/dist
tasks.register<Exec>("bundleWebAssets") {
    commandLine("python3", "../build_apk.py")
}
