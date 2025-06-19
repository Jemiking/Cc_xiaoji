#!/bin/bash

# 为所有模块创建测试目录结构和示例测试文件

echo "开始创建测试目录结构..."

# 定义模块列表
modules=(
    "app"
    "core/common"
    "core/ui"
    "core/database"
    "core/network"
    "feature/todo"
    "feature/habit"
    "feature/ledger"
    "feature/schedule"
    "shared/user"
    "shared/sync"
    "shared/backup"
    "shared/notification"
)

# 为每个模块创建测试目录
for module in "${modules[@]}"; do
    echo "处理模块: $module"
    
    # 创建测试目录
    test_dir="$module/src/test/java/com/ccxiaoji/${module//\//.}"
    android_test_dir="$module/src/androidTest/java/com/ccxiaoji/${module//\//.}"
    
    mkdir -p "$test_dir"
    mkdir -p "$android_test_dir"
    
    echo "  创建目录: $test_dir"
    echo "  创建目录: $android_test_dir"
done

echo "测试目录结构创建完成！"