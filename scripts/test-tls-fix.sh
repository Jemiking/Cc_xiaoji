#!/bin/bash

echo "========================================"
echo "测试TLS配置修复"
echo "========================================"

# 清理Gradle缓存
echo "1. 清理Gradle守护进程..."
./gradlew --stop

echo -e "\n2. 清理项目构建缓存..."
rm -rf .gradle/
rm -rf build/
rm -rf app/build/

echo -e "\n3. 测试Gradle配置..."
./gradlew --version

echo -e "\n4. 尝试下载依赖（使用--info查看详细信息）..."
echo "如果需要查看TLS握手详细信息，请取消gradle.properties中第59行的注释"
./gradlew dependencies --info | grep -E "(TLS|SSL|https|Download)" | head -20

echo -e "\n5. 尝试编译项目..."
./gradlew :app:compileDebugKotlin

echo -e "\n========================================"
echo "测试完成！"
echo "如果仍有问题，请检查以下内容："
echo "1. 取消gradle.properties第59行注释查看SSL调试信息"
echo "2. 检查代理设置是否正确（当前配置了127.0.0.1:7897）"
echo "3. 尝试临时禁用代理：注释掉第58-62行"
echo "========================================"