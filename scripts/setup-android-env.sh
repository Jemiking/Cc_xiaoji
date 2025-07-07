#!/bin/bash
# 设置Android环境变量的脚本

export ANDROID_HOME=/mnt/c/Users/Hua/AppData/Local/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin

echo "Android环境变量已设置："
echo "ANDROID_HOME=$ANDROID_HOME"
echo "PATH包含Android工具"

# 验证Android SDK
if [ -d "$ANDROID_HOME" ]; then
    echo "✅ Android SDK目录存在"
    ls -la $ANDROID_HOME/build-tools/ | head -5
    echo "..."
else
    echo "❌ Android SDK目录不存在"
fi