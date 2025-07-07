#!/bin/bash

# 快速编译脚本，跳过网络下载
export GRADLE_USER_HOME=$HOME/.gradle
export GRADLE_HOME=$HOME/.gradle/wrapper/dists/gradle-8.9-bin/8fd2a6/gradle-8.9

# 确保Gradle已经解压
if [ ! -f "$GRADLE_HOME/bin/gradle" ]; then
    echo "Gradle未找到，正在解压..."
    cd $HOME/.gradle/wrapper/dists/gradle-8.9-bin/8fd2a6/
    unzip -o -q gradle-8.9-bin.zip
    touch gradle-8.9-bin.zip.ok
fi

# 使用本地Gradle编译
cd /mnt/d/kotlin/Cc_xiaoji
echo "开始编译plan模块..."
$GRADLE_HOME/bin/gradle :feature:plan:compileDebugKotlin --no-daemon --offline