#!/bin/bash

echo "清理 JVM Toolchain 相关缓存..."

# 清理 Gradle 缓存
echo "1. 清理 Gradle 缓存"
./gradlew clean
rm -rf .gradle

# 清理 build-logic 构建缓存
echo "2. 清理 build-logic 构建缓存"
rm -rf build-logic/.gradle
rm -rf build-logic/convention/build

# 清理所有模块的构建目录
echo "3. 清理所有模块构建目录"
find . -type d -name "build" -not -path "./node_modules/*" -exec rm -rf {} + 2>/dev/null || true

# 清理 IDE 缓存文件
echo "4. 清理 IDE 缓存"
rm -rf .idea/gradle.xml
rm -rf .idea/modules.xml

echo "清理完成！"
echo ""
echo "下一步："
echo "1. 在 Android Studio 中执行 File -> Sync Project with Gradle Files"
echo "2. 运行 ./gradlew verifyJvmVersion 验证 JVM 版本"
echo "3. 运行 ./gradlew build 进行编译"