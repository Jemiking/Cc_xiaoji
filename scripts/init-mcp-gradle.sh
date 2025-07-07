#!/bin/bash

# 初始化MCP测试项目的Gradle环境
echo "=== 初始化MCP Gradle环境 ==="

MCP_PROJECT="/mnt/d/kotlin/mcp-kotlin-compiler/test-project"

# 检查目录
if [ ! -d "$MCP_PROJECT" ]; then
    echo "错误：找不到MCP测试项目目录"
    exit 1
fi

cd "$MCP_PROJECT"

# 设置Gradle权限
echo "1. 设置gradlew执行权限..."
chmod +x gradlew

# 显示Gradle版本
echo "2. 检查Gradle版本..."
./gradlew --version

# 下载依赖
echo "3. 下载项目依赖..."
./gradlew dependencies --refresh-dependencies

# 编译项目
echo "4. 编译测试项目..."
./gradlew clean build

# 显示结果
if [ $? -eq 0 ]; then
    echo ""
    echo "✅ MCP Gradle环境初始化成功！"
    echo "Gradle和所有依赖已经下载完成。"
    echo ""
    echo "现在可以使用MCP编译器了："
    echo "- 编译项目: compile_project"
    echo "- 编译文件: compile_file"
    echo "- 增量编译: incremental_compile"
else
    echo ""
    echo "❌ 初始化失败，请检查网络连接和Gradle配置"
fi