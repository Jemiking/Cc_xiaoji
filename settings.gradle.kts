pluginManagement {
    repositories {
        // 添加阿里云镜像
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
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

// Check Java version
val javaVersion = JavaVersion.current()
if (javaVersion.majorVersion.toInt() > 11) {
    println("⚠️ WARNING: Current Java version is $javaVersion")
    println("⚠️ This project requires Java 11")
    println("⚠️ Please change Gradle JDK in Android Studio settings")
}