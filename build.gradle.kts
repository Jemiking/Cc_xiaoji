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

plugins {
    id("com.android.application") version "8.2.1" apply false
    id("org.jetbrains.kotlin.android") version "1.9.21" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "1.9.21-1.0.15" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.21" apply false
}

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