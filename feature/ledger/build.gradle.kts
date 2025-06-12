plugins {
    id("ccxiaoji.android.feature")
}

android {
    namespace = "com.ccxiaoji.feature.ledger"

    sourceSets {
        getByName("main") {
            java.srcDirs(
                "api/src/main/kotlin",
                "data/src/main/kotlin",
                "domain/src/main/kotlin",
                "presentation/src/main/kotlin"
            )
        }
    }
}

dependencies {
    // Feature-specific dependencies
    implementation(project(":core:database"))
    implementation(project(":core:data"))
    
    // Shared modules
    implementation(project(":shared:user"))
    
    // Ledger-specific dependencies
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.graphics)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    
    // Additional test dependencies
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}