pluginManagement {
    repositories {
        // 阿里云镜像优先
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 备用源
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        // 添加阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        google()
        mavenCentral()
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
include(":shared:backup")
include(":shared:notification")

// Feature modules
include(":feature:todo")
include(":feature:habit")
include(":feature:ledger")

// Check Java version
val javaVersion = JavaVersion.current()
println("ℹ️ Current Java version: $javaVersion")
println("ℹ️ Gradle version 8.9 supports Java 21")