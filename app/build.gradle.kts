plugins {
    id("ccxiaoji.android.application")
    id("ccxiaoji.android.application.compose")
    id("ccxiaoji.android.hilt")
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.ccxiaoji.app"

    defaultConfig {
        applicationId = "com.ccxiaoji.app"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    
    buildTypes {
        create("benchmark") {
            signingConfig = getByName("debug").signingConfig
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }
    
    kotlinOptions {
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xjvm-default=all"
        )
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:database"))
    implementation(project(":core:data"))
    
    // Feature modules
    implementation(project(":feature:todo"))
    implementation(project(":feature:habit"))
    implementation(project(":feature:ledger"))
    
    // Shared modules
    implementation(project(":shared:user"))
    implementation(project(":shared:sync"))
    implementation(project(":shared:backup"))
    implementation(project(":shared:notification"))
    
    // 以下依赖已由 Convention Plugin 提供
    // Activity - 由 ccxiaoji.android.application.compose 提供
    // implementation(libs.androidx.activity.compose)
    
    // Navigation - 由 ccxiaoji.android.application.compose 提供
    // implementation(libs.androidx.navigation.compose)
    // implementation(libs.hilt.navigation.compose)
    
    // App-specific dependencies
    implementation(libs.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    
    // Room for local database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Retrofit for API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    
    // Kotlin Serialization
    implementation(libs.kotlinx.serialization.json)
    
    // DataStore for preferences
    implementation(libs.androidx.datastore.preferences)
    
    // WorkManager for background sync
    implementation(libs.androidx.work.runtime.ktx)
    
    // Security
    implementation(libs.androidx.security.crypto)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    // compose.ui.test.junit4 已由 Convention Plugin 提供
    // androidTestImplementation(libs.compose.ui.test.junit4)
    
    // Performance Testing
    androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    androidTestImplementation(libs.androidx.benchmark.junit4)
    androidTestImplementation(libs.androidx.test.uiautomator)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
}