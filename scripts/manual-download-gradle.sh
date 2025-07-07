#!/bin/bash

# 手动下载Gradle的脚本（当自动下载失败时使用）

GRADLE_VERSION="8.5"
GRADLE_USER_HOME="${HOME}/.gradle"
GRADLE_DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

echo "=== 手动下载并安装Gradle ${GRADLE_VERSION} ==="
echo ""

# 创建目录
echo "1. 创建Gradle目录..."
mkdir -p "$GRADLE_DIST_DIR"

# 选择下载源
echo ""
echo "2. 选择下载源："
echo "   [1] 腾讯云镜像（推荐）"
echo "   [2] 阿里云镜像"
echo "   [3] 官方源"
echo ""
read -p "请选择 (1-3): " choice

case $choice in
    1)
        DOWNLOAD_URL="https://mirrors.cloud.tencent.com/gradle/gradle-${GRADLE_VERSION}-bin.zip"
        ;;
    2)
        DOWNLOAD_URL="https://maven.aliyun.com/repository/public/gradle/gradle-${GRADLE_VERSION}-bin.zip"
        ;;
    3)
        DOWNLOAD_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
        ;;
    *)
        echo "无效选择，使用腾讯云镜像"
        DOWNLOAD_URL="https://mirrors.cloud.tencent.com/gradle/gradle-${GRADLE_VERSION}-bin.zip"
        ;;
esac

# 下载文件
TEMP_FILE="/tmp/gradle-${GRADLE_VERSION}-bin.zip"
echo ""
echo "3. 开始下载..."
echo "   下载地址: $DOWNLOAD_URL"
echo "   保存位置: $TEMP_FILE"
echo ""

if command -v wget > /dev/null; then
    wget -c -t 3 --timeout=30 "$DOWNLOAD_URL" -O "$TEMP_FILE"
elif command -v curl > /dev/null; then
    curl -L -C - --retry 3 --connect-timeout 30 "$DOWNLOAD_URL" -o "$TEMP_FILE"
else
    echo "错误：需要安装wget或curl"
    exit 1
fi

if [ ! -f "$TEMP_FILE" ]; then
    echo "错误：下载失败"
    exit 1
fi

# 验证文件
echo ""
echo "4. 验证下载文件..."
if file "$TEMP_FILE" | grep -q "Zip archive"; then
    echo "   ✅ 文件验证通过"
else
    echo "   ❌ 文件损坏，请重新下载"
    exit 1
fi

# 创建Gradle wrapper目录结构
echo ""
echo "5. 安装Gradle..."

# 计算文件hash（Gradle wrapper需要）
if command -v sha256sum > /dev/null; then
    HASH=$(sha256sum "$TEMP_FILE" | cut -d' ' -f1 | head -c 6)
elif command -v shasum > /dev/null; then
    HASH=$(shasum -a 256 "$TEMP_FILE" | cut -d' ' -f1 | head -c 6)
else
    HASH="manual"
fi

# 创建目标目录
FINAL_DIR="$GRADLE_DIST_DIR/$HASH"
mkdir -p "$FINAL_DIR"

# 复制文件
cp "$TEMP_FILE" "$FINAL_DIR/gradle-${GRADLE_VERSION}-bin.zip"

# 解压
echo "   解压Gradle..."
cd "$FINAL_DIR"
unzip -q "gradle-${GRADLE_VERSION}-bin.zip"

# 创建.ok标记文件（表示下载完成）
touch "$FINAL_DIR/gradle-${GRADLE_VERSION}-bin.zip.ok"

# 清理临时文件
rm -f "$TEMP_FILE"

echo ""
echo "✅ Gradle ${GRADLE_VERSION} 安装成功！"
echo ""
echo "安装位置: $FINAL_DIR"
echo ""
echo "现在可以重新运行MCP编译器了。"

# 可选：设置GRADLE_HOME环境变量
echo ""
echo "可选：添加到环境变量"
echo "export GRADLE_HOME=$FINAL_DIR/gradle-${GRADLE_VERSION}"
echo "export PATH=\$GRADLE_HOME/bin:\$PATH"