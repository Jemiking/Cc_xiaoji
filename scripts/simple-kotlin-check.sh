#!/bin/bash

# 简单的Kotlin语法检查脚本
# 用于MCP环境快速验证代码正确性

echo "=================================="
echo "简单Kotlin语法检查"
echo "=================================="

# 设置基本环境
export ANDROID_HOME=/mnt/c/Users/Hua/AppData/Local/Android/Sdk
export GRADLE_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m"

# 选择要检查的模块（默认检查最近修改的）
if [ -z "$1" ]; then
    echo "使用方法: ./scripts/simple-kotlin-check.sh <模块名>"
    echo "例如: ./scripts/simple-kotlin-check.sh feature:ledger"
    echo ""
    echo "可用模块:"
    echo "  app"
    echo "  core:common"
    echo "  core:ui"
    echo "  core:database"
    echo "  core:network"
    echo "  feature:todo"
    echo "  feature:habit"
    echo "  feature:ledger"
    echo "  feature:schedule"
    echo "  feature:plan"
    exit 0
fi

MODULE=$1
echo "检查模块: $MODULE"

# 尝试编译，忽略Android特定的任务
./gradlew :${MODULE}:compileDebugKotlin \
    -x lint \
    -x lintDebug \
    -x processDebugManifest \
    -x processDebugResources \
    -x generateDebugBuildConfig \
    -x mergeDebugResources \
    -x createDebugCompatibleScreenManifests \
    --console=plain \
    --no-daemon \
    2>&1 | grep -E "(e:|w:|错误:|警告:|Error:|Warning:|FAILED|BUILD SUCCESSFUL)"

# 检查结果
if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo -e "\n✅ 模块 $MODULE 语法检查通过！"
else
    echo -e "\n❌ 模块 $MODULE 存在编译错误，请查看上面的错误信息。"
fi