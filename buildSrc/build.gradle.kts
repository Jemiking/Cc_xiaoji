plugins {
    `kotlin-dsl`
}

repositories {
    // 优先使用镜像以规避网络/TLS 抖动
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
    maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }

    // 官方源置后兜底
    google()
    gradlePluginPortal()
    mavenCentral()

    // JetBrains 公共仓库（部分 Kotlin 工件）
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx/maven") }
}

dependencies {
    implementation("com.android.tools.build:gradle:8.3.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.48.1")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:1.9.24-1.0.20")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.9.24")
}
