#!/bin/bash

echo "=================================="
echo "修复WSL中的AAPT问题"
echo "=================================="

SDK_PATH="/mnt/c/Users/Hua/AppData/Local/Android/Sdk"
BUILD_TOOLS_34="$SDK_PATH/build-tools/34.0.0"

# 检查35.0.0版本（可能有Linux版本）
if [ -f "$SDK_PATH/build-tools/35.0.0/aapt" ]; then
    echo "找到35.0.0版本的aapt，创建符号链接..."
    ln -sf "$SDK_PATH/build-tools/35.0.0/aapt" "$BUILD_TOOLS_34/aapt"
    ln -sf "$SDK_PATH/build-tools/35.0.0/aapt2" "$BUILD_TOOLS_34/aapt2"
    echo "✅ 符号链接创建成功"
else
    echo "尝试使用33.0.2版本..."
    if [ -f "$SDK_PATH/build-tools/33.0.2/aapt" ]; then
        ln -sf "$SDK_PATH/build-tools/33.0.2/aapt" "$BUILD_TOOLS_34/aapt"
        ln -sf "$SDK_PATH/build-tools/33.0.2/aapt2" "$BUILD_TOOLS_34/aapt2"
        echo "✅ 使用33.0.2版本的aapt"
    else
        echo "❌ 找不到合适的aapt版本"
    fi
fi

# 列出build-tools/34.0.0目录内容
echo -e "\nbuild-tools/34.0.0目录内容："
ls -la "$BUILD_TOOLS_34" | grep aapt