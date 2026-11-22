import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}
val signingReady = listOf("SIGNING_KEYSTORE_PATH","SIGNING_STORE_PASSWORD","SIGNING_KEY_ALIAS","SIGNING_KEY_PASSWORD")
    .all { !System.getenv(it).isNullOrBlank() }
android {
    signingConfigs {
        if (signingReady) create("ciRelease") {
            storeFile = file(System.getenv("SIGNING_KEYSTORE_PATH"))
            storePassword = System.getenv("SIGNING_STORE_PASSWORD")
            keyAlias = System.getenv("SIGNING_KEY_ALIAS")
            keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
        }
    }
    namespace = "com.mhmmad.balldropchainreaction"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.mhmmad.balldropchainreaction"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }
    buildFeatures { compose = true; buildConfig = true }
    androidResources { noCompress += "wav" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            if (signingReady) signingConfig = signingConfigs.getByName("ciRelease")
            // No signing config is checked into source control.
        }
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}
dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.04.01"))
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("org.jbox2d:jbox2d-library:2.2.1.1")
    testImplementation("junit:junit:4.13.2")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}
