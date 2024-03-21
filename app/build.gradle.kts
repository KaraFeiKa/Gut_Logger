plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.testkotlin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.testkotlin"
        minSdk = 29
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.yandex.android:maps.mobile:4.4.0-lite")

    implementation ("com.google.android.gms:play-services-location:21.2.0")

    implementation ("org.osmdroid:osmdroid-android:6.1.18")
    implementation ("com.github.MKergall:osmbonuspack:6.9.0")

    implementation("com.github.MikeOrtiz:TouchImageView:1.4.1")

    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.opencsv:opencsv:5.9")
    implementation ("com.github.lau1944:Zoom-Drag-Rotate-ImageView:1.0.0")
}