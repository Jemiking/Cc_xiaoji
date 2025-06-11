plugins {
    id("ccxiaoji.android.library")
    id("ccxiaoji.android.library.compose")
}

android {
    namespace = "com.ccxiaoji.core.ui"
    
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    
    // Expose Compose as API for feature modules
    api(libs.compose.ui.graphics)
    api(libs.compose.material.icons.extended)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}