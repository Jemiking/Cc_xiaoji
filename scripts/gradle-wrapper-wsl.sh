#!/bin/bash

# WSL Android编译Wrapper
# 这个脚本处理WSL环境中使用Windows Android SDK的问题

echo "=================================="
echo "WSL Android编译环境配置"
echo "=================================="

# 设置环境变量
export ANDROID_HOME=/mnt/c/Users/Hua/AppData/Local/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME

# 使用Wine运行Windows的aapt（如果安装了wine）
# export AAPT_EXEC="wine $ANDROID_HOME/build-tools/34.0.0/aapt.exe"
# export AAPT2_EXEC="wine $ANDROID_HOME/build-tools/34.0.0/aapt2.exe"

# 临时解决方案：忽略aapt检查，只编译Kotlin代码
export ANDROID_AAPT_IGNORE=true

# 设置Gradle属性来跳过某些Android检查
export GRADLE_OPTS="-Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dandroid.builder.sdkDownload=false"

# 禁用某些Android任务
SKIP_TASKS="-x processDebugResources -x mergeDebugResources -x generateDebugBuildConfig"

echo "尝试只编译Kotlin代码（跳过资源处理）..."

# 停止现有守护进程
./gradlew --stop

# 编译命令
if [ "$1" == "full" ]; then
    echo "执行完整编译..."
    ./gradlew :app:compileDebugKotlin
else
    echo "执行快速编译（跳过资源）..."
    ./gradlew :app:compileDebugKotlinWithJavac $SKIP_TASKS --no-build-cache
fi

echo -e "\n=================================="
echo "编译完成"
echo "===================================="