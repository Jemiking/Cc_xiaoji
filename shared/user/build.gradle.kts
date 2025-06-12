plugins {
    id("ccxiaoji.android.library")
    id("ccxiaoji.android.hilt")
}

android {
    namespace = "com.ccxiaoji.shared.user"

    sourceSets {
        getByName("main") {
            java.srcDirs(
                "api/src/main/kotlin",
                "data/src/main/kotlin",
                "domain/src/main/kotlin"
            )
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))
    
    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    
    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}