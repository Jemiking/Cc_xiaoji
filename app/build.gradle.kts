plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

android {
    namespace = "com.ccxiaoji.app"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "com.ccxiaoji.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 250  // v2.5.0 - FastExcel重构版本，完全重写导入导出功能
        versionName = "2.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // 版本特性标记
        buildConfigField("String", "VERSION_FEATURES", "\"EXCEL_NATIVE,FASTEXCEL,STREAM_PROCESSING,HIGH_PERFORMANCE\"")
        buildConfigField("boolean", "FASTEXCEL_ENABLED", "true")
        buildConfigField("boolean", "INTEGRATION_TESTS_ENABLED", "true")
        buildConfigField("String", "BACKUP_FORMAT_VERSION", "\"3.0\"")
        buildConfigField("String", "SUPPORTED_FORMATS", "\"xlsx\"")
        buildConfigField("long", "BUILD_TIMESTAMP", "${System.currentTimeMillis()}L")
    }
    
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // 已添加Hilt ProGuard规则
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xjvm-default=all"
        )
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}

dependencies {
    // Module dependencies - Only direct feature and shared modules needed
    implementation(project(":shared:user"))
    implementation(project(":shared:sync"))
    implementation(project(":shared:backup"))
    implementation(project(":shared:notification"))
    implementation(project(":feature:todo"))
    implementation(project(":feature:habit"))
    implementation(project(":feature:ledger"))
    implementation(project(":feature:schedule"))
    implementation(project(":feature:plan"))
    
    // Core modules needed for app-specific functionality
    implementation(project(":core:network")) // For TokenProvider
    implementation(project(":core:database")) // For database access
    implementation(project(":core:common")) // For common utilities
    implementation(project(":core:ui")) // For UI components and theme
    
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Hilt for DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)
    
    // Room (only needed for schema export)
    ksp(libs.androidx.room.compiler)
    
    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // Kotlin DateTime
    implementation(libs.kotlinx.datetime)
    
    // Gson
    implementation(libs.gson)
    
    // Excel处理 (FastExcel)
    implementation("org.dhatim:fastexcel:0.15.7")
    implementation("org.dhatim:fastexcel-reader:0.15.7")
    
    // CSV处理
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    
    // Desugaring for Java 8+ APIs
    coreLibraryDesugaring(libs.android.tools.desugar)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.truth)
    testImplementation(libs.robolectric)
    
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

