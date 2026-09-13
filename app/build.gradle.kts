import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

val monetization = Properties().apply {
    val file = rootProject.file("monetization.properties")
    if (file.exists()) file.inputStream().use(::load)
}

fun cfg(name: String, fallback: String): String = monetization.getProperty(name, fallback)
fun quoted(value: String): String = "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

android {
    namespace = "com.noxforgestudios.noxspeed"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.noxforgestudios.noxspeed"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        manifestPlaceholders["admobAppId"] = cfg("ADMOB_APP_ID", "ca-app-pub-3940256099942544~3347511713")
        buildConfigField("String", "ADMOB_BANNER_ID", quoted(cfg("ADMOB_BANNER_ID", "ca-app-pub-3940256099942544/6300978111")))
        buildConfigField("String", "ADMOB_INTERSTITIAL_ID", quoted(cfg("ADMOB_INTERSTITIAL_ID", "ca-app-pub-3940256099942544/1033173712")))
        buildConfigField("String", "ADMOB_REWARDED_ID", quoted(cfg("ADMOB_REWARDED_ID", "ca-app-pub-3940256099942544/5224354917")))
        buildConfigField("String", "PREMIUM_PRODUCT_ID", quoted(cfg("PREMIUM_PRODUCT_ID", "premium_lifetime")))
        buildConfigField("String", "SUPPORT_EMAIL", quoted(cfg("SUPPORT_EMAIL", "")))
        buildConfigField("String", "PRIVACY_URL", quoted(cfg("PRIVACY_URL", "https://noxforgestudios.netlify.app/noxspeed/privacy-es.html")))
        buildConfigField("String", "TERMS_URL", quoted(cfg("TERMS_URL", "https://noxforgestudios.netlify.app/noxspeed/terms-es.html")))
        buildConfigField("String", "PLAY_STORE_URL", quoted(cfg("PLAY_STORE_URL", "https://play.google.com/store/apps/details?id=com.noxforgestudios.noxspeed")))
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            buildConfigField("boolean", "DEMO_GPS_AVAILABLE", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "DEMO_GPS_AVAILABLE", "false")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.navigation:navigation-compose:2.9.6")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.core:core-splashscreen:1.2.0")

    implementation("androidx.compose.ui:ui:1.12.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.12.1")
    implementation("androidx.compose.foundation:foundation:1.12.1")
    implementation("androidx.compose.animation:animation:1.12.1")
    implementation("androidx.compose.material3:material3:1.4.0")
    debugImplementation("androidx.compose.ui:ui-tooling:1.12.1")

    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    implementation("com.android.billingclient:billing-ktx:9.1.0")
    implementation("com.google.android.gms:play-services-ads:25.4.0")
    implementation("com.google.android.ump:user-messaging-platform:4.0.0")
    implementation("com.google.android.play:review-ktx:2.0.2")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation("junit:junit:4.13.2")
}
