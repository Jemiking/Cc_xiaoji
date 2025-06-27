// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        // 阿里云镜像优先
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 备用源
        google()
        mavenCentral()
    }
}

// 插件已在buildSrc中定义，无需重复声明

allprojects {
    repositories {
        // 阿里云镜像优先
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        // 备用源
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}