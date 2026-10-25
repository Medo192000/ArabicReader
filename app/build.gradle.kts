plugins {
    id("com.android.application") version "8.7.3"
}

android {
    namespace = "com.medo.arabicreader"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.medo.arabicreader"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity:1.9.3")
    implementation("com.rmtheis:tess-two:9.1.0")
}
