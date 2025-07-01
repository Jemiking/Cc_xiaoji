#!/bin/bash
# Claude Code会话恢复脚本
# 用途：快速恢复工作状态并验证环境

set -e

echo "=================================="
echo "Claude Code会话恢复脚本"
echo "=================================="

# 1. 显示当前状态
echo ""
echo "📊 当前Git状态："
git branch --show-current
git status --short

# 2. 检查MCP配置
echo ""
echo "🔧 MCP配置状态："
claude mcp list | grep kotlin-compiler || echo "⚠️  MCP未配置"

# 3. 检查Gradle
echo ""
echo "📦 Gradle状态："
if [ -d ~/.gradle/wrapper/dists/gradle-8.9-bin ]; then
    echo "✅ Gradle 8.9 已安装"
else
    echo "❌ Gradle 8.9 未安装"
fi

# 4. 显示版本迁移状态
echo ""
echo "📋 版本迁移状态："
echo "已完成模块："
for module in shared/user shared/sync shared/backup shared/notification; do
    if grep -q "libs.versions.compileSdk" "$module/build.gradle.kts" 2>/dev/null; then
        echo "  ✅ $module"
    else
        echo "  ❌ $module"
    fi
done

# 5. 显示待办事项
echo ""
echo "📝 下一步操作："
echo "1. 测试MCP: 使用mcp__kotlin-compiler__compile_project工具"
echo "2. 检查其他模块: grep -r 'compileSdk = 34' . --include='*.kts'"
echo "3. 运行编译: ./gradlew clean build"

echo ""
echo "💡 提示：查看 CLAUDE_SESSION_RESTORE.md 获取详细信息"
echo "=================================="