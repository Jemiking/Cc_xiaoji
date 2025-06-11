package com.ccxiaoji.convention

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.findByType
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

/**
 * 统一配置 JVM Toolchain，确保所有任务使用相同的 JDK 版本
 * 
 * 根据 Claude.md 要求，项目统一使用 JDK 17
 */
internal fun Project.configureJvmToolchain() {
    // 配置 Java toolchain
    extensions.findByType<JavaPluginExtension>()?.apply {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
    
    // 配置 Kotlin Android toolchain
    extensions.findByType<KotlinAndroidProjectExtension>()?.apply {
        jvmToolchain(17)
    }
    
    // 配置 Kotlin JVM toolchain（用于纯 Kotlin 模块）
    extensions.findByType<KotlinJvmProjectExtension>()?.apply {
        jvmToolchain(17)
    }
    
    // 配置通用 Kotlin toolchain
    extensions.findByType<KotlinProjectExtension>()?.apply {
        jvmToolchain(17)
    }
}