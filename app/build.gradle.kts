import java.util.Properties
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
    id("org.jetbrains.kotlin.plugin.serialization")
}

// 读取签名配置（支持 env 与 tools/secrets 位置）
val keystoreProperties = Properties()
val keystoreFilesToTry = listOf(
    rootProject.file("tools/secrets/keystore.properties"),
    rootProject.file("keystore.properties"),
)
keystoreFilesToTry.firstOrNull { it.exists() }?.let { f ->
    f.inputStream().use { keystoreProperties.load(it) }
}

fun envOrProp(env: String, prop: String): String? =
    System.getenv(env) ?: keystoreProperties.getProperty(prop)

val vStoreFilePath: String? = System.getenv("KEYSTORE_FILE") ?: keystoreProperties.getProperty("storeFile")
val vStorePassword: String? = envOrProp("KEYSTORE_PASSWORD", "storePassword")
val vKeyAlias: String? = envOrProp("KEY_ALIAS", "keyAlias")
val vKeyPassword: String? = envOrProp("KEY_PASSWORD", "keyPassword")

val hasSigning: Boolean = run {
    val ok = !vStoreFilePath.isNullOrBlank() &&
            File(vStoreFilePath!!).exists() &&
            !vStorePassword.isNullOrBlank() &&
            !vKeyAlias.isNullOrBlank() &&
            !vKeyPassword.isNullOrBlank()
    if (!ok) println("ℹ️ Release signing not fully configured; falling back to debug signing.")
    ok
}

android {
    namespace = "com.ccxiaoji.app"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        applicationId = "com.ccxiaoji.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 250  // v2.5.0 - 模块化导出架构
        versionName = "2.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        
        // 版本特性标记
        buildConfigField("String", "VERSION_FEATURES", "\"MODULAR_EXPORT,CSV_EXPORT,HIGH_PERFORMANCE\"")
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

    signingConfigs {
        create("release") {
            if (hasSigning) {
                keyAlias = vKeyAlias
                keyPassword = vKeyPassword
                storeFile = file(vStoreFilePath!!)
                storePassword = vStorePassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // 已添加Hilt ProGuard规则
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // 显示/隐藏 Demo 功能的显式开关（Release 默认关闭）
            buildConfigField("boolean", "SHOW_STYLE_DEMO", "false")
            signingConfig = if (hasSigning) {
                signingConfigs.getByName("release")
            } else {
                println("警告: 未配置发布签名参数，使用 debug 签名")
                signingConfigs.getByName("debug")
            }
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            
            // Debug版本标识，允许与Release版本同时安装
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            
            // 调试功能配置
            buildConfigField("String", "BUILD_TYPE", "\"DEBUG\"")
            buildConfigField("boolean", "LOGGING_ENABLED", "true")
            buildConfigField("boolean", "DEBUG_MODE", "true")
            // Debug 打开 Demo 功能入口
            buildConfigField("boolean", "SHOW_STYLE_DEMO", "true")
            
            // 构建性能优化（加快debug构建速度）
            isJniDebuggable = false
            isRenderscriptDebuggable = false
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
    
    // CSV处理
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:1.9.3")
    
    // Google Play Services (位置服务)
    implementation("com.google.android.gms:play-services-location:21.1.0")
    
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

