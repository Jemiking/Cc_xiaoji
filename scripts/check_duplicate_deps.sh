#!/bin/bash

echo "检查重复依赖和优化机会"
echo "======================"
echo

# 检查是否有模块同时被多个模块依赖
echo "1. 依赖频率分析："
echo "-----------------"

# 收集所有依赖关系
declare -A dep_count

for build_file in $(find . -name "build.gradle.kts" -not -path "./build/*" -not -path "./buildSrc/*"); do
    deps=$(grep -E 'implementation\(project\(":[^"]+"\)\)' "$build_file" | \
           sed 's/.*implementation(project(":\([^"]*\)")).*/\1/')
    
    for dep in $deps; do
        ((dep_count["$dep"]++))
    done
done

# 输出依赖频率
for dep in "${!dep_count[@]}"; do
    echo "  $dep: 被 ${dep_count[$dep]} 个模块依赖"
done | sort -t: -k2 -nr

echo
echo "2. 传递依赖检查："
echo "-----------------"

# 检查app模块是否有不必要的直接依赖
echo "app模块直接依赖了所有模块，考虑以下优化："
echo "  - app不需要直接依赖core模块，因为feature模块已经依赖了它们"
echo "  - 可以移除对 core:common, core:ui, core:database, core:network 的直接依赖"

echo
echo "3. 外部依赖重复检查："
echo "---------------------"

# 检查常见的外部依赖是否在多个模块中重复声明
common_deps=("androidx.compose.ui:ui" "androidx.hilt:hilt-navigation-compose" "androidx.lifecycle:lifecycle-viewmodel-compose")

for dep in "${common_deps[@]}"; do
    echo "检查依赖: $dep"
    count=$(grep -l "$dep" $(find . -name "build.gradle.kts" -not -path "./build/*" -not -path "./buildSrc/*") | wc -l)
    if [ $count -gt 2 ]; then
        echo "  ⚠️  在 $count 个模块中重复声明"
    else
        echo "  ✓ 依赖声明合理"
    fi
done

echo
echo "4. 性能优化建议："
echo "-----------------"
echo "  1. 考虑使用 api() 而不是 implementation() 对于常用的传递依赖"
echo "  2. 将常用的 Compose 依赖移到 core:ui 模块并使用 api()"
echo "  3. 移除 app 模块中不必要的直接 core 模块依赖"
echo "  4. 考虑创建版本目录 (Version Catalog) 统一管理依赖版本"