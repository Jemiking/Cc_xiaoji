#!/bin/bash

echo "移除 feature 模块中的重复 Compose 依赖"
echo "===================================="

# 需要从 feature 模块中移除的依赖行
deps_to_remove=(
    'implementation(platform("androidx.compose:compose-bom:'
    'implementation("androidx.compose.ui:ui")'
    'implementation("androidx.compose.ui:ui-graphics")'
    'implementation("androidx.compose.ui:ui-tooling-preview")'
    'implementation("androidx.compose.material3:material3")'
    'implementation("androidx.compose.material:material-icons-extended")'
)

# 处理每个 feature 模块
for module in feature/todo feature/habit feature/ledger; do
    build_file="$module/build.gradle.kts"
    
    if [ -f "$build_file" ]; then
        echo "处理模块: $module"
        
        # 创建临时文件
        temp_file=$(mktemp)
        
        # 读取文件并过滤掉重复的依赖
        while IFS= read -r line; do
            should_remove=false
            
            for dep in "${deps_to_remove[@]}"; do
                if [[ "$line" == *"$dep"* ]]; then
                    should_remove=true
                    echo "  移除: $line"
                    break
                fi
            done
            
            if [ "$should_remove" = false ]; then
                echo "$line" >> "$temp_file"
            fi
        done < "$build_file"
        
        # 替换原文件
        mv "$temp_file" "$build_file"
        echo "  ✓ 完成"
        echo
    fi
done

echo "优化完成！"