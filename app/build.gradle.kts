plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.composeHotReload)


    // Hilt
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")


    // Serialization
    kotlin("plugin.serialization") version "2.2.0"
}

android {
    namespace = "com.sujith.pocket"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.sujith.pocket"
        minSdk = 26
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    // Hilt
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-compiler:2.57.1")

    // Hilt Navigation Compose (for hiltViewModel)
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")


    // Material Icons
    implementation("androidx.compose.material:material-icons-extended:1.7.8")


    // Integration with Activity lifecycle
    implementation("androidx.activity:activity-compose:1.12.0")

    // Lifecycle & ViewModel integration with Compose
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4") // Safely collect Flows in Composables
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4") // Use viewModel() directly in Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.4") // Coroutine support in ViewModel (viewModelScope)

    // Hilt
//    implementation("io.insert-koin:koin-android:4.1.1")
//    implementation("io.insert-koin:koin-annotations:2.3.0")
//    ksp("io.insert-koin:koin-ksp-compiler:2.3.0")


    // Retrofit
//    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Room
    val room_version = "2.8.3"

    implementation("androidx.room:room-runtime:$room_version")

    // If this project uses any Kotlin source, use Kotlin Symbol Processing (KSP)
    // See Add the KSP plugin to your project
    ksp("androidx.room:room-compiler:$room_version")

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:$room_version")

    // optional - Test helpers
    testImplementation("androidx.room:room-testing:$room_version")

    // optional - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")


    // datastore
    implementation("androidx.datastore:datastore-preferences:1.1.1") // Key-value storage (like SharedPreferences)

    // Jetpack Navigation for Compose (Stable Nav2)
    implementation("androidx.navigation:navigation-compose:2.9.6")


    // Async
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2") // Core coroutine support
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2") // Dispatchers.Main, lifecycle support

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")


    // Splash screen
    implementation("androidx.core:core-splashscreen:1.2.0")

    // Web scrap
    implementation("org.jsoup:jsoup:1.21.2")

    // mat 3
    implementation("androidx.compose.material3:material3:1.4.0")


}