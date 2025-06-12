package com.ccxiaoji.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * 配置 Kotlin Android 项目的通用设置 - Application版本
 */
internal fun Project.configureKotlinAndroid(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {
        compileSdk = 34

        defaultConfig {
            minSdk = 26
        }
        
        // compileOptions 由 JVM Toolchain 自动处理，无需手动配置
    }

    configureKotlinAndroidCommon()
}

/**
 * 配置 Kotlin Android 项目的通用设置 - Library版本
 */
internal fun Project.configureKotlinAndroid(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.apply {
        compileSdk = 34

        defaultConfig {
            minSdk = 26
        }
        
        // compileOptions 由 JVM Toolchain 自动处理，无需手动配置
    }

    configureKotlinAndroidCommon()
}

/**
 * 配置 Kotlin Android 项目的通用部分
 */
private fun Project.configureKotlinAndroidCommon() {
    // 配置 JVM Toolchain
    configureJvmToolchain()
    
    // 配置 Kotlin 编译选项
    configureKotlin()

    dependencies {
        add("implementation", libs.findLibrary("androidx.core.ktx").get())
        add("implementation", libs.findLibrary("kotlinx.coroutines.android").get())
        add("implementation", libs.findLibrary("kotlinx.datetime").get())
    }
}

/**
 * 配置 Kotlin 编译选项
 */
private fun Project.configureKotlin() {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            // jvmTarget 由 JVM Toolchain 自动设置，无需手动配置
            // 仅配置编译器参数
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-opt-in=kotlin.RequiresOptIn",
                "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                "-opt-in=kotlinx.coroutines.FlowPreview"
            )
        }
    }
}