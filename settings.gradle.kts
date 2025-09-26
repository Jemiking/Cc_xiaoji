pluginManagement {
    repositories {
        // 先走镜像，降低 TLS/网络波动影响
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }

        // 官方源兜底
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 优先使用官方仓库
        mavenCentral()
        google()
        // 阿里云Maven镜像（备用）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // JitPack仓库（用于poi-android）
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "CcXiaoJi"
include(":app")

// Core modules
include(":core:common")
include(":core:ui")
include(":core:database")
include(":core:network")

// Shared modules
include(":shared:user")
include(":shared:sync")
include(":shared:notification")

// Feature modules
include(":feature:todo")
include(":feature:habit")
include(":feature:ledger")
include(":feature:schedule")
include(":feature:plan")

// Check Java version
val javaVersion = JavaVersion.current()
println("ℹ️ Current Java version: $javaVersion")
println("ℹ️ Gradle version 8.9 supports Java 21")
