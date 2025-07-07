pluginManagement {
    repositories {
        // 直接使用官方源，通过代理访问
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        // 直接使用官方源，通过代理访问
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
include(":feature:schedule")
include(":feature:plan")

// Check Java version
val javaVersion = JavaVersion.current()
println("ℹ️ Current Java version: $javaVersion")
println("ℹ️ Gradle version 8.9 supports Java 21")