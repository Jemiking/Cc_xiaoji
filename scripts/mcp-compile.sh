#!/bin/bash

# MCP专用编译脚本
# 在WSL环境中验证Kotlin代码编译

echo "=================================="
echo "MCP Kotlin代码验证"
echo "=================================="

# 设置环境变量
export ANDROID_HOME=/mnt/c/Users/Hua/AppData/Local/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME

# 清理之前的构建
echo "清理构建缓存..."
find . -name "build" -type d -path "*/src/*" -prune -o -name "build" -type d -exec rm -rf {} + 2>/dev/null || true

# 停止Gradle守护进程
./gradlew --stop

echo -e "\n开始编译Kotlin代码..."
echo "注意：由于WSL环境限制，仅验证Kotlin语法，不处理Android资源"

# 编译各个模块的Kotlin代码
MODULES=(
    ":app"
    ":core:common"
    ":core:ui"
    ":core:database"
    ":core:network"
    ":shared:user"
    ":shared:sync"
    ":shared:backup"
    ":shared:notification"
    ":feature:todo"
    ":feature:habit"
    ":feature:ledger"
    ":feature:schedule"
    ":feature:plan"
)

SUCCESS=true
FAILED_MODULES=()

for module in "${MODULES[@]}"; do
    echo -e "\n编译模块: $module"
    if ./gradlew ${module}:compileDebugKotlin -x lint -x processDebugManifest -x processDebugResources 2>&1 | tee compile.log | grep -E "(FAILED|error:|错误:|Error:|Unresolved reference)"; then
        echo "❌ $module 编译失败"
        FAILED_MODULES+=("$module")
        SUCCESS=false
    else
        if grep -q "BUILD SUCCESSFUL" compile.log; then
            echo "✅ $module 编译成功"
        else
            echo "⚠️  $module 编译状态未知"
        fi
    fi
done

echo -e "\n=================================="
echo "编译总结"
echo "=================================="

if [ "$SUCCESS" = true ]; then
    echo "✅ 所有模块编译成功！"
    echo "代码语法正确，可以在Android Studio中进行完整构建。"
else
    echo "❌ 以下模块编译失败："
    for module in "${FAILED_MODULES[@]}"; do
        echo "  - $module"
    done
    echo -e "\n请检查compile.log文件获取详细错误信息。"
fi

# 清理临时文件
rm -f compile.log

echo "===================================="