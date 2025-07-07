#!/bin/bash

echo "=== 最终测试MCP编译器 ==="
echo ""

MCP_DIR="/mnt/d/kotlin/mcp-kotlin-compiler"
TEST_PROJECT="$MCP_DIR/test-project"

# 1. 显示当前配置
echo "1. 当前网络配置："
echo "   代理设置："
grep -E "systemProp\.(http|https)\.proxy" "$TEST_PROJECT/gradle.properties" | head -5
echo ""

# 2. 测试Gradle下载
echo "2. 测试Gradle Wrapper下载..."
cd "$TEST_PROJECT"
if timeout 60 ./gradlew --version > /tmp/gradle-test.log 2>&1; then
    echo "   ✅ Gradle下载成功！"
    ./gradlew --version
else
    echo "   ❌ Gradle下载失败"
    echo "   错误信息："
    tail -n 20 /tmp/gradle-test.log
fi
echo ""

# 3. 测试项目编译
echo "3. 测试项目编译..."
if [ -d "$TEST_PROJECT/gradle" ]; then
    if timeout 120 ./gradlew clean build > /tmp/build-test.log 2>&1; then
        echo "   ✅ 项目编译成功！"
    else
        echo "   ❌ 项目编译失败"
        echo "   错误信息："
        grep -E "error:|ERROR|FAILED" /tmp/build-test.log | head -10
    fi
fi
echo ""

# 4. 测试MCP服务器
echo "4. 验证MCP服务器..."
if [ -f "$MCP_DIR/dist/server.js" ]; then
    echo "   ✅ MCP服务器文件存在"
    echo "   位置: $MCP_DIR/dist/server.js"
else
    echo "   ❌ MCP服务器文件不存在"
    echo "   请运行: cd $MCP_DIR && npm run build"
fi

echo ""
echo "=== 测试总结 ==="
echo ""
echo "解决网络问题的步骤："
echo "1. ✅ 配置了国内镜像源（阿里云、腾讯云）"
echo "2. ✅ 增加了网络超时时间（120秒）"
echo "3. ✅ 自动检测并配置了代理（127.0.0.1:7897）"
echo "4. ✅ 创建了全局Gradle配置"
echo ""
echo "如果仍有问题："
echo "- 检查代理是否正常工作"
echo "- 尝试手动下载：./scripts/manual-download-gradle.sh"
echo "- 查看详细日志：/tmp/gradle-test.log 和 /tmp/build-test.log"