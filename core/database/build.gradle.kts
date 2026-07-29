plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    // Using simple plugins, room-compiler will use annotationProcessor for simplicity or kapt if added.
    // We will list room-compiler dependency.
}

android {
    namespace = "com.aura.core.database"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    
    // We will use room-compiler via annotationProcessor for compilation.
    annotationProcessor(libs.room.compiler)
    
    implementation(libs.hilt.android)
    annotationProcessor(libs.hilt.compiler)
    
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
