#!/bin/bash

echo "=== 修复Gradle下载问题 ==="
echo ""

# 1. 清理Gradle缓存
echo "1. 清理本地Gradle缓存..."
rm -rf .gradle/
rm -rf ~/.gradle/caches/
rm -rf ~/.gradle/wrapper/dists/gradle-8.9-bin/

# 2. 创建全局Gradle配置
echo "2. 创建全局Gradle配置..."
mkdir -p ~/.gradle/

cat > ~/.gradle/gradle.properties << 'EOF'
# 全局代理配置
systemProp.http.proxyHost=127.0.0.1
systemProp.http.proxyPort=7897
systemProp.https.proxyHost=127.0.0.1
systemProp.https.proxyPort=7897
systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.aliyun.com|*.tencent.com

# 网络超时配置
systemProp.http.connectionTimeout=300000
systemProp.http.socketTimeout=300000
systemProp.https.connectionTimeout=300000
systemProp.https.socketTimeout=300000

# Gradle下载超时
org.gradle.internal.http.connectionTimeout=300000
org.gradle.internal.http.socketTimeout=300000
EOF

echo "✅ 全局配置已创建"

# 3. 手动下载Gradle（使用代理）
echo ""
echo "3. 尝试手动下载Gradle 8.9..."
GRADLE_URL="https://mirrors.cloud.tencent.com/gradle/gradle-8.9-bin.zip"
GRADLE_DIST_DIR="$HOME/.gradle/wrapper/dists/gradle-8.9-bin"
GRADLE_ZIP="/tmp/gradle-8.9-bin.zip"

if [ ! -f "$HOME/.gradle/wrapper/dists/gradle-8.9-bin/*/gradle-8.9-bin.zip" ]; then
    echo "   正在下载..."
    curl -x 127.0.0.1:7897 -L -o "$GRADLE_ZIP" "$GRADLE_URL" || \
    wget -e use_proxy=yes -e http_proxy=127.0.0.1:7897 -e https_proxy=127.0.0.1:7897 -O "$GRADLE_ZIP" "$GRADLE_URL"
    
    if [ -f "$GRADLE_ZIP" ]; then
        echo "   ✅ 下载成功"
        # 创建目录结构
        HASH=$(echo -n "$GRADLE_URL" | sha256sum | cut -c1-6)
        mkdir -p "$GRADLE_DIST_DIR/$HASH"
        cp "$GRADLE_ZIP" "$GRADLE_DIST_DIR/$HASH/"
        cd "$GRADLE_DIST_DIR/$HASH/"
        unzip -q gradle-8.9-bin.zip
        touch gradle-8.9-bin.zip.ok
        echo "   ✅ Gradle已安装"
    else
        echo "   ❌ 下载失败"
    fi
else
    echo "   ✅ Gradle 8.9已存在"
fi

# 4. 测试Gradle
echo ""
echo "4. 测试Gradle..."
cd /mnt/d/kotlin/Cc_xiaoji
./gradlew --version

echo ""
echo "修复完成！现在可以尝试编译了。"