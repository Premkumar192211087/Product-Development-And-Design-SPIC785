plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

if (project.file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.example.stockpilot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.stockpilot"
        minSdk = 24
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    dataBinding {
        enable = true
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        disable.add("MissingTranslation")
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.core:core:1.12.0")
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Material Design - Use consistent version
    implementation("com.google.android.material:material:1.11.0")

    // UI Components
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.drawerlayout:drawerlayout:1.2.0")

    // Image loading
    implementation(libs.github.glide)
    implementation(libs.swiperefreshlayout)
    annotationProcessor(libs.compiler)
    implementation("com.squareup.picasso:picasso:2.8")

    // Networking
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Utilities
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("commons-io:commons-io:2.11.0")
    implementation(libs.work.runtime)

    // PDF Generation
    implementation("com.itextpdf:itext7-core:7.1.15")

    // QR Code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Firebase - Only include if google-services.json exists
    if (file("google-services.json").exists()) {
        implementation(libs.firebase.messaging)
    }

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
