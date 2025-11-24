plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.medimanager"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.medimanager"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.coordinatorlayout)
    implementation(libs.circleimageview)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
