#!/bin/bash
# 清理构建缓存脚本

echo "清理构建缓存..."

# 清理 build 目录
find . -name "build" -type d -exec rm -rf {} + 2>/dev/null || true

# 清理 .gradle 目录
rm -rf .gradle/ 2>/dev/null || true

# 清理各模块的 .gradle 目录
find . -name ".gradle" -type d -exec rm -rf {} + 2>/dev/null || true

# 清理 IDEA 缓存
rm -rf .idea/caches/ 2>/dev/null || true

# 清理 KSP 生成的文件
find . -name "kspCaches" -type d -exec rm -rf {} + 2>/dev/null || true

echo "缓存清理完成！"
echo "请重新运行 ./gradlew build 来重建项目"