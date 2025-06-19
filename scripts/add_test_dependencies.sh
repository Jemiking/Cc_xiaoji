#!/bin/bash

# 为所有模块添加测试依赖的脚本

echo "开始为所有模块添加测试依赖..."

# 定义要添加的测试依赖
TEST_DEPS='    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("com.google.truth:truth:1.1.5")
    
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")'

# 查找所有的build.gradle.kts文件（排除根目录的）
find . -name "build.gradle.kts" -not -path "./build.gradle.kts" | while read file; do
    echo "处理文件: $file"
    
    # 检查是否已经有完整的测试依赖
    if grep -q "io.mockk:mockk" "$file"; then
        echo "  跳过 - 已有MockK依赖"
        continue
    fi
    
    # 检查是否是Android库模块
    if grep -q "com.android.library\|com.android.application" "$file"; then
        echo "  更新测试依赖..."
        
        # 备份原文件
        cp "$file" "$file.bak"
        
        # 使用sed替换测试依赖部分
        # 首先尝试找到现有的测试部分并替换
        if grep -q "// Testing" "$file"; then
            # 有Testing注释，替换整个测试部分直到文件结束的}
            sed -i '/\/\/ Testing/,/^}$/c\'"$TEST_DEPS"'\n}' "$file"
        else
            # 没有Testing注释，在最后一个}前插入
            sed -i '/^}$/i\'"$TEST_DEPS" "$file"
        fi
        
        echo "  完成"
    else
        echo "  跳过 - 不是Android模块"
    fi
done

echo "测试依赖添加完成！"