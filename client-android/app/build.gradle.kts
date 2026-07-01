plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val appsFlyerDevKey = providers.gradleProperty("APPSFLYER_DEV_KEY").orNull ?: ""
val facebookAppId = providers.gradleProperty("FACEBOOK_APP_ID").orNull ?: ""
val appLinkHost = providers.gradleProperty("APP_LINK_HOST").orNull ?: "link.simplewebview.local"

android {
    namespace = "com.example.simplewebview"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.simplewebview"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "APPSFLYER_DEV_KEY", "\"$appsFlyerDevKey\"")
        buildConfigField("String", "FACEBOOK_APP_ID", "\"$facebookAppId\"")
        manifestPlaceholders["appLinkHost"] = appLinkHost
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":attribution"))
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
