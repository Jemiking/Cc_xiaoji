#!/bin/bash

echo "模块依赖分析报告"
echo "=================="
echo

# 分析每个模块的依赖
for build_file in $(find . -name "build.gradle.kts" -not -path "./build/*" -not -path "./buildSrc/*" | sort); do
    module_name=$(dirname "$build_file" | sed 's|^\./||')
    
    if [ "$module_name" != "." ]; then
        echo "模块: $module_name"
        echo "依赖的模块:"
        
        # 提取 implementation(project(":xxx")) 依赖
        grep -E 'implementation\(project\(":[^"]+"\)\)' "$build_file" | \
            sed 's/.*implementation(project(":\([^"]*\)")).*/  - \1/' | sort | uniq
        
        # 统计外部依赖数量
        external_deps=$(grep -E '(implementation|api)\(' "$build_file" | \
            grep -v 'project(' | wc -l)
        echo "  外部依赖数量: $external_deps"
        echo
    fi
done

echo "依赖关系图（简化版）:"
echo "===================="
echo "app"
echo "├── feature:todo"
echo "├── feature:habit"
echo "├── feature:ledger"
echo "├── shared:user"
echo "├── shared:sync"
echo "├── shared:backup"
echo "├── shared:notification"
echo "├── core:common"
echo "├── core:ui"
echo "├── core:database"
echo "└── core:network"
echo
echo "feature modules → shared modules → core modules"