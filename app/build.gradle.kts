plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.multitradex"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.multitradex"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(libs.room.common.jvm)
    implementation(libs.room.runtime.android)
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom))
    implementation (libs.room.runtime)
    implementation(libs.swiperefreshlayout)
    annotationProcessor(libs.room.compiler)

    implementation (libs.retrofit2.retrofit)
    implementation (libs.converter.gson)
    implementation (libs.imagepicker)
    implementation (libs.kotlinx.coroutines.android)
    //noinspection UseTomlInstead
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor(libs.compiler.v4160)
    implementation (libs.lifecycle.livedata.ktx)
    implementation (libs.lifecycle.viewmodel.ktx)
    implementation (libs.dotsindicator)
    // Shimmer
    implementation (libs.shimmer)
    implementation ("com.google.android.material:material:1.12.0")



    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}