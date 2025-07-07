#!/bin/bash
# 修复MCP Kotlin编译器的脚本

MCP_DIR="/mnt/d/kotlin/mcp-kotlin-compiler"
TEST_PROJECT_DIR="$MCP_DIR/test-project"

echo "=== 修复MCP Kotlin编译器 ==="

# 1. 检查目录是否存在
if [ ! -d "$MCP_DIR" ]; then
    echo "错误：MCP目录不存在：$MCP_DIR"
    exit 1
fi

# 2. 进入MCP目录并构建TypeScript项目
echo "步骤1：构建TypeScript项目..."
cd "$MCP_DIR"
npm run build

# 3. 检查构建结果
if [ ! -f "$MCP_DIR/dist/server.js" ]; then
    echo "错误：TypeScript构建失败，找不到dist/server.js"
    exit 1
fi

# 4. 测试编译Kotlin项目
echo "步骤2：测试编译Kotlin项目..."
cd "$TEST_PROJECT_DIR"

# 确保gradlew有执行权限
chmod +x gradlew

# 清理并构建
./gradlew clean build

# 5. 检查编译结果
if [ $? -eq 0 ]; then
    echo "✅ MCP Kotlin编译器修复成功！"
    echo "测试项目编译通过。"
else
    echo "❌ 测试项目编译失败"
    echo "请检查错误日志：$MCP_DIR/error.log"
fi

# 6. 显示编译输出
echo ""
echo "=== 编译输出 ==="
./gradlew compileKotlin --quiet

echo ""
echo "修复完成。MCP服务器配置位于：$MCP_DIR/claude-code-config.json"