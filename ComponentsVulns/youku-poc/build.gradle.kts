plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.componentsvulns.youkupoc"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.componentsvulns.youkupoc"
        minSdk = 29
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
