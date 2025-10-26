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
    // 注意：不要在 buildSrc 中引入 AGP/Kotlin/Hilt/KSP 等插件工件，避免将“未知版本”的插件放入构建类路径，
    // 造成各模块通过 alias 解析带版本插件时的冲突（AlreadyOnClasspathPluginResolver）。
    // 如果后续需要为 buildSrc 添加通用库，请仅添加与构建脚本实现相关的普通依赖。
}
