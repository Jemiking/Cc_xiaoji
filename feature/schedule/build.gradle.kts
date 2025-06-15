plugins {
    id("ccxiaoji.android.feature")
}

android {
    namespace = "com.ccxiaoji.feature.schedule"

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
    implementation(project(":shared:notification"))
    implementation(project(":shared:backup"))
    
    // WorkManager for notifications
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.hilt.work)
    
    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
}