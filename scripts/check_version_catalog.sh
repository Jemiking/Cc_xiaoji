#!/bin/bash

# 检查版本目录使用情况的脚本
echo "=== 检查版本目录使用情况 ==="
echo ""

# 定义要检查的模块
modules=(
    "core/network"
    "core/ui"
    "shared/backup"
    "shared/notification"
    "shared/sync"
    "shared/user"
)

# 检查每个模块
for module in "${modules[@]}"; do
    echo "检查模块: $module"
    build_file="$module/build.gradle.kts"
    
    if [ -f "$build_file" ]; then
        # 检查是否还有硬编码的版本号（格式如 "group:artifact:version"）
        hardcoded=$(grep -E 'implementation\("|api\("|testImplementation\("|androidTestImplementation\("|ksp\("' "$build_file" | grep -E ':[0-9]+\.[0-9]+' || true)
        
        if [ -z "$hardcoded" ]; then
            echo "  ✓ 已完成版本目录迁移"
        else
            echo "  ✗ 发现硬编码版本："
            echo "$hardcoded" | while IFS= read -r line; do
                echo "    $line"
            done
        fi
    else
        echo "  ✗ 找不到文件: $build_file"
    fi
    echo ""
done

echo "=== 检查完成 ==="