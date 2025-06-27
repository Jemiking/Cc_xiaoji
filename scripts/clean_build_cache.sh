#!/bin/bash

echo "清理构建缓存..."

# 清理Gradle构建缓存
./gradlew clean

# 清理KSP缓存
rm -rf app/build/generated/ksp
rm -rf app/build/ksp

# 清理Kotlin编译缓存
rm -rf app/build/tmp/kotlin-classes
rm -rf app/build/kotlin

# 清理整个build目录
rm -rf app/build
rm -rf build

# 清理.gradle目录（本地构建缓存）
rm -rf .gradle

echo "构建缓存清理完成！"
echo "现在可以重新编译项目了。"