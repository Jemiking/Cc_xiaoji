#!/bin/bash
# 创建标准化feature模块的脚本

# 检查参数
if [ $# -eq 0 ]; then
    echo "❌ 错误: 请提供模块名称"
    echo "用法: ./create_feature_module.sh <module_name>"
    echo "示例: ./create_feature_module.sh calendar"
    exit 1
fi

MODULE_NAME=$1
MODULE_PATH="feature/$MODULE_NAME"
PACKAGE_PATH="com/ccxiaoji/feature/$MODULE_NAME"

# 检查模块是否已存在
if [ -d "$MODULE_PATH" ]; then
    echo "❌ 错误: 模块 $MODULE_NAME 已存在"
    exit 1
fi

echo "🚀 开始创建feature模块: $MODULE_NAME"

# 创建目录结构
echo "📁 创建目录结构..."
mkdir -p "$MODULE_PATH/src/main/kotlin/$PACKAGE_PATH"/{api,data/{local/{dao,entity},remote,repository},di,domain/{model,repository,usecase},presentation/{screen,component,viewmodel,navigation}}
mkdir -p "$MODULE_PATH/src/test/kotlin/$PACKAGE_PATH"/{data,domain,presentation}
mkdir -p "$MODULE_PATH/src/androidTest/kotlin/$PACKAGE_PATH"

# 创建build.gradle.kts
echo "📝 创建build.gradle.kts..."
cat > "$MODULE_PATH/build.gradle.kts" << 'EOF'
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.ccxiaoji.feature.MODULE_NAME"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildToolsVersion = libs.versions.buildTools.get()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs = listOf(
            "-Xjsr305=strict",
            "-Xjvm-default=all",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }
    
    buildFeatures {
        compose = true
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }
}

dependencies {
    // Core模块
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:database"))
    
    // Shared模块
    implementation(project(":shared:user"))
    
    // Android核心
    implementation(libs.androidx.core.ktx)
    implementation(libs.kotlin.coroutines)
    implementation(libs.kotlin.datetime)
    
    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Desugaring
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    
    // 测试依赖
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.truth)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.robolectric)
    
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.compose.ui.test.junit4)
    
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
EOF

# 替换模块名称
sed -i "s/MODULE_NAME/$MODULE_NAME/g" "$MODULE_PATH/build.gradle.kts"

# 创建AndroidManifest.xml
echo "📝 创建AndroidManifest.xml..."
cat > "$MODULE_PATH/src/main/AndroidManifest.xml" << EOF
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Feature模块不需要声明任何内容，除非有特殊需求 -->
</manifest>
EOF

# 创建proguard规则文件
touch "$MODULE_PATH/proguard-rules.pro"
touch "$MODULE_PATH/consumer-rules.pro"

# 创建API接口
echo "📝 创建API接口..."
cat > "$MODULE_PATH/src/main/kotlin/$PACKAGE_PATH/api/${MODULE_NAME^}Api.kt" << EOF
package com.ccxiaoji.feature.$MODULE_NAME.api

/**
 * ${MODULE_NAME^}模块对外API接口
 * 
 * 该接口定义了其他模块可以访问的功能
 */
interface ${MODULE_NAME^}Api {
    /**
     * 导航到${MODULE_NAME^}主页面
     */
    fun navigateTo${MODULE_NAME^}()
    
    // TODO: 添加其他公开API方法
}
EOF

# 创建DI模块
echo "📝 创建DI模块..."
cat > "$MODULE_PATH/src/main/kotlin/$PACKAGE_PATH/di/${MODULE_NAME^}Module.kt" << EOF
package com.ccxiaoji.feature.$MODULE_NAME.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ${MODULE_NAME^}Module {
    
    // TODO: 添加依赖提供方法
}
EOF

# 创建示例Screen
echo "📝 创建示例Screen..."
cat > "$MODULE_PATH/src/main/kotlin/$PACKAGE_PATH/presentation/screen/${MODULE_NAME^}Screen.kt" << EOF
package com.ccxiaoji.feature.$MODULE_NAME.presentation.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ${MODULE_NAME^}Screen(
    viewModel: ${MODULE_NAME^}ViewModel = hiltViewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${MODULE_NAME^}") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Welcome to ${MODULE_NAME^} Module",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}
EOF

# 创建ViewModel
echo "📝 创建ViewModel..."
cat > "$MODULE_PATH/src/main/kotlin/$PACKAGE_PATH/presentation/viewmodel/${MODULE_NAME^}ViewModel.kt" << EOF
package com.ccxiaoji.feature.$MODULE_NAME.presentation.screen

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ${MODULE_NAME^}ViewModel @Inject constructor(
    // TODO: 注入依赖
) : ViewModel() {
    
    // TODO: 实现ViewModel逻辑
}
EOF

# 创建Navigation
echo "📝 创建Navigation..."
cat > "$MODULE_PATH/src/main/kotlin/$PACKAGE_PATH/presentation/navigation/${MODULE_NAME^}Navigation.kt" << EOF
package com.ccxiaoji.feature.$MODULE_NAME.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.ccxiaoji.feature.$MODULE_NAME.presentation.screen.${MODULE_NAME^}Screen

const val ${MODULE_NAME}_ROUTE = "${MODULE_NAME}"

fun NavController.navigateTo${MODULE_NAME^}() {
    navigate(${MODULE_NAME}_ROUTE)
}

fun NavGraphBuilder.${MODULE_NAME}Screen() {
    composable(route = ${MODULE_NAME}_ROUTE) {
        ${MODULE_NAME^}Screen()
    }
}
EOF

# 创建README
echo "📝 创建README.md..."
cat > "$MODULE_PATH/README.md" << EOF
# Feature ${MODULE_NAME^}

## 概述
${MODULE_NAME^}功能模块，提供[请填写功能描述]。

## 架构
本模块遵循Clean Architecture + MVVM架构模式：

- **api/**: 对外公开的API接口
- **data/**: 数据层实现
  - **local/**: 本地数据存储（Room）
  - **remote/**: 远程数据访问
  - **repository/**: 数据仓库实现
- **di/**: 依赖注入配置
- **domain/**: 业务逻辑层
  - **model/**: 业务模型
  - **repository/**: 仓库接口
  - **usecase/**: 业务用例
- **presentation/**: 展示层
  - **screen/**: Compose页面
  - **component/**: 可复用组件
  - **viewmodel/**: 视图模型
  - **navigation/**: 导航相关

## 主要功能
- [ ] 功能1
- [ ] 功能2
- [ ] 功能3

## 依赖关系
- core-common: 基础工具类
- core-ui: UI组件和主题
- core-database: 数据库基础设施
- shared-user: 用户管理

## 使用方式
\`\`\`kotlin
// 在app模块中注册导航
${MODULE_NAME}Screen()

// 导航到${MODULE_NAME^}页面
navController.navigateTo${MODULE_NAME^}()
\`\`\`
EOF

# 创建示例测试
echo "📝 创建示例测试..."
cat > "$MODULE_PATH/src/test/kotlin/$PACKAGE_PATH/ExampleUnitTest.kt" << EOF
package com.ccxiaoji.feature.$MODULE_NAME

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertThat(2 + 2).isEqualTo(4)
    }
}
EOF

# 更新settings.gradle.kts
echo "📝 更新settings.gradle.kts..."
if ! grep -q ":feature:$MODULE_NAME" "settings.gradle.kts"; then
    # 在最后一个feature模块后添加新模块
    last_feature=$(grep ':feature:' settings.gradle.kts | tail -1)
    sed -i "/$last_feature/a include(\":feature:$MODULE_NAME\")" settings.gradle.kts
    echo "✅ 已添加到settings.gradle.kts"
else
    echo "⚠️ 模块已在settings.gradle.kts中"
fi

# 完成提示
echo "
✅ 模块创建完成！

📁 模块位置: $MODULE_PATH

下一步：
1. 在app模块的build.gradle.kts中添加依赖:
   implementation(project(\":feature:$MODULE_NAME\"))

2. 在app模块的导航中注册:
   ${MODULE_NAME}Screen()

3. 在app模块的DI中提供API实现:
   @Provides
   fun provide${MODULE_NAME^}Api(): ${MODULE_NAME^}Api = ${MODULE_NAME^}ApiImpl()

4. 开始开发你的功能！

提示：运行 ./gradlew :feature:$MODULE_NAME:build 验证模块配置
"