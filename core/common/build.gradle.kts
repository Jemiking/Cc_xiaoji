plugins {
    id("ccxiaoji.android.library")
    id("ccxiaoji.android.hilt")
}

android {
    namespace = "com.ccxiaoji.core.common"
    
    defaultConfig {
        consumerProguardFiles("consumer-rules.pro")
    }
}

dependencies {
    // Additional common dependencies
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
}