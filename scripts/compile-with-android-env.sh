#!/bin/bash

echo "=================================="
echo "MCP Android编译环境设置"
echo "=================================="

# 设置Android环境变量
export ANDROID_HOME=/mnt/c/Users/Hua/AppData/Local/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools:$ANDROID_HOME/tools/bin:$ANDROID_HOME/build-tools/34.0.0

echo "ANDROID_HOME=$ANDROID_HOME"

# 验证Android SDK
if [ -d "$ANDROID_HOME" ]; then
    echo "✅ Android SDK目录存在"
    
    # 检查必要的组件
    if [ -d "$ANDROID_HOME/platforms/android-34" ]; then
        echo "✅ Android Platform 34 存在"
    else
        echo "❌ Android Platform 34 不存在"
    fi
    
    if [ -f "$ANDROID_HOME/build-tools/34.0.0/aapt2" ]; then
        echo "✅ Build Tools 34.0.0 存在"
    else
        echo "❌ Build Tools 34.0.0 不存在"
    fi
else
    echo "❌ Android SDK目录不存在"
    exit 1
fi

echo -e "\n开始编译项目..."

# 停止现有的Gradle守护进程
./gradlew --stop

# 使用环境变量编译
ANDROID_HOME=$ANDROID_HOME ./gradlew :app:compileDebugKotlin

echo -e "\n=================================="
echo "编译完成"
echo "=================================="