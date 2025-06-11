package com.ccxiaoji.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * 配置 Compose 相关设置 - Application版本
 */
internal fun Project.configureAndroidCompose(
    applicationExtension: ApplicationExtension,
) {
    applicationExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("compose.compiler").get().toString()
        }
    }

    configureComposeCommon()
}

/**
 * 配置 Compose 相关设置 - Library版本
 */
internal fun Project.configureAndroidCompose(
    libraryExtension: LibraryExtension,
) {
    libraryExtension.apply {
        buildFeatures {
            compose = true
        }

        composeOptions {
            kotlinCompilerExtensionVersion = libs.findVersion("compose.compiler").get().toString()
        }
    }

    configureComposeCommon()
}

/**
 * 配置 Compose 通用依赖
 * 
 * 包含所有 Compose 项目常用的依赖：
 * - Compose UI 核心组件
 * - Material Design 3
 * - 生命周期集成（包括 collectAsStateWithLifecycle）
 * - 导航组件
 * - Hilt 集成
 * - 调试和测试工具
 */
private fun Project.configureComposeCommon() {
    dependencies {
        val bom = libs.findLibrary("compose.bom").get()
        add("implementation", platform(bom))
        add("androidTestImplementation", platform(bom))
        
        // Compose 核心依赖
        add("implementation", libs.findLibrary("compose.ui").get())
        add("implementation", libs.findLibrary("compose.ui.graphics").get())
        add("implementation", libs.findLibrary("compose.ui.tooling.preview").get())
        add("implementation", libs.findLibrary("compose.material3").get())
        add("implementation", libs.findLibrary("compose.material.icons.extended").get())
        
        // Compose 生命周期和状态管理
        add("implementation", libs.findLibrary("androidx.lifecycle.runtime.compose").get())
        add("implementation", libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())
        
        // Compose 导航
        add("implementation", libs.findLibrary("androidx.navigation.compose").get())
        add("implementation", libs.findLibrary("hilt.navigation.compose").get())
        
        // Compose Activity 集成
        add("implementation", libs.findLibrary("androidx.activity.compose").get())
        
        // 调试工具
        add("debugImplementation", libs.findLibrary("compose.ui.tooling").get())
        add("debugImplementation", libs.findLibrary("compose.ui.test.manifest").get())
        
        // 测试依赖
        add("androidTestImplementation", libs.findLibrary("compose.ui.test.junit4").get())
    }
}