#!/bin/bash

# 修复MCP Kotlin编译器网络问题的综合脚本

echo "=== 修复MCP网络配置 ==="
echo ""

MCP_PROJECT="/mnt/d/kotlin/mcp-kotlin-compiler/test-project"
GRADLE_USER_HOME="${HOME}/.gradle"

# 1. 创建Gradle全局配置目录
echo "1. 配置Gradle全局设置..."
mkdir -p "$GRADLE_USER_HOME"

# 2. 创建或更新init.gradle.kts (全局配置)
cat > "$GRADLE_USER_HOME/init.gradle.kts" << 'EOF'
// Gradle全局初始化脚本 - 配置国内镜像

allprojects {
    repositories {
        // 清除默认仓库
        clear()
        
        // 阿里云镜像（推荐）
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/central") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        
        // 腾讯云镜像（备选）
        maven { url = uri("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/") }
        
        // 华为云镜像（备选）
        maven { url = uri("https://repo.huaweicloud.com/repository/maven/") }
        
        // 原始仓库（最后尝试）
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

settingsEvaluated {
    pluginManagement {
        repositories {
            clear()
            maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
            maven { url = uri("https://maven.aliyun.com/repository/public") }
            gradlePluginPortal()
            mavenCentral()
        }
    }
}
EOF

echo "✅ Gradle全局镜像配置完成"

# 3. 配置Gradle Wrapper镜像
echo ""
echo "2. 配置Gradle Wrapper下载源..."

# 创建gradle-wrapper配置
mkdir -p "$GRADLE_USER_HOME/wrapper/dists"

# 4. 检查DNS配置
echo ""
echo "3. 检查网络连接..."

# 测试连接
echo -n "   测试阿里云镜像: "
if curl -s --connect-timeout 5 https://maven.aliyun.com > /dev/null; then
    echo "✅ 可访问"
else
    echo "❌ 无法访问"
fi

echo -n "   测试腾讯云镜像: "
if curl -s --connect-timeout 5 https://mirrors.cloud.tencent.com > /dev/null; then
    echo "✅ 可访问"
else
    echo "❌ 无法访问"
fi

# 5. 清理缓存
echo ""
echo "4. 清理Gradle缓存..."
if [ -d "$MCP_PROJECT/.gradle" ]; then
    rm -rf "$MCP_PROJECT/.gradle"
    echo "   ✅ 项目缓存已清理"
fi

# 6. 测试Gradle下载
echo ""
echo "5. 测试Gradle Wrapper下载..."
cd "$MCP_PROJECT"
if [ -f "./gradlew" ]; then
    chmod +x ./gradlew
    echo "   正在下载Gradle..."
    if timeout 120 ./gradlew --version > /dev/null 2>&1; then
        echo "   ✅ Gradle下载成功"
    else
        echo "   ❌ Gradle下载失败"
        echo ""
        echo "   可能的解决方案："
        echo "   1. 检查网络代理设置"
        echo "   2. 手动下载Gradle并放入: $GRADLE_USER_HOME/wrapper/dists/"
        echo "   3. 使用VPN或修改DNS"
    fi
fi

# 7. 显示代理配置提示
echo ""
echo "=== 代理配置提示 ==="
echo "如果您使用代理，请编辑: $MCP_PROJECT/gradle.properties"
echo "取消注释并修改以下配置："
echo "  systemProp.http.proxyHost=你的代理地址"
echo "  systemProp.http.proxyPort=你的代理端口"
echo "  systemProp.https.proxyHost=你的代理地址"
echo "  systemProp.https.proxyPort=你的代理端口"

# 8. 创建网络诊断脚本
cat > "$MCP_PROJECT/diagnose-network.sh" << 'EOF'
#!/bin/bash
echo "=== Gradle网络诊断 ==="
echo ""
echo "1. DNS解析测试:"
nslookup services.gradle.org
echo ""
echo "2. 连接测试:"
curl -I https://services.gradle.org/distributions/gradle-8.5-bin.zip
echo ""
echo "3. 镜像测试:"
curl -I https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip
echo ""
echo "4. 当前代理设置:"
env | grep -i proxy
EOF

chmod +x "$MCP_PROJECT/diagnose-network.sh"

echo ""
echo "=== 修复完成 ==="
echo ""
echo "已完成的配置："
echo "✅ 1. Gradle全局镜像配置"
echo "✅ 2. 项目镜像源配置"
echo "✅ 3. 网络超时设置"
echo "✅ 4. 缓存清理"
echo ""
echo "后续步骤："
echo "1. 运行: cd $MCP_PROJECT && ./gradlew build"
echo "2. 如果仍有问题，运行: ./diagnose-network.sh"
echo "3. 查看详细日志: ./gradlew build --info"