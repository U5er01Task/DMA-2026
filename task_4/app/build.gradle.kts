plugins {
    id("com.android.application")
}

android {
    namespace = "ru.kulikov.pr4"
    compileSdk = 35

    defaultConfig {
        applicationId = "ru.kulikov.pr4"
        minSdk = 26
        // 34, а не 35: на Android 15 при targetSdk 35 включается обязательный edge-to-edge,
        // и экраны с ActionBar надо дополнительно настраивать под системные отступы
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
}
