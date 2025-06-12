pluginManagement {
    includeBuild("build-logic")
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
include(":core:data")

// Feature modules
include(":feature:todo")
include(":feature:habit")
include(":feature:ledger")
include(":feature:schedule")

// Shared modules
include(":shared:user")
include(":shared:sync")
include(":shared:backup")
include(":shared:backup:api")
include(":shared:backup:data")
include(":shared:backup:domain")
include(":shared:notification")
include(":shared:notification:api")
include(":shared:notification:data")
include(":shared:notification:domain")

// Check Java version
val javaVersion = JavaVersion.current()
println("ℹ️ Current Java version: $javaVersion")
println("ℹ️ Gradle version 8.9 supports Java 21")