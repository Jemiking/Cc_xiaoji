#!/bin/bash
# MCP编译器预处理脚本
# 用于生成MCP编译器所需的中间文件

echo "🔧 准备MCP编译环境..."

# 停止所有Gradle守护进程，避免并发问题
echo "⏹️  停止Gradle守护进程..."
./gradlew --stop

# 生成必要的中间文件
echo "📦 生成Manifest文件..."
./gradlew processDebugManifest --parallel

echo "🏗️  生成BuildConfig文件..."
./gradlew :app:generateDebugBuildConfig

echo "📋 生成资源文件..."
./gradlew mergeDebugResources --parallel

echo "✅ MCP编译环境准备完成！"
echo "现在可以使用MCP编译器了"