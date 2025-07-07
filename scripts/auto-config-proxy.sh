#!/bin/bash

# 自动检测并配置代理的脚本

echo "=== 自动配置代理 ==="
echo ""

GRADLE_PROPERTIES="/mnt/d/kotlin/mcp-kotlin-compiler/test-project/gradle.properties"

# 检测系统代理
detect_proxy() {
    local proxy=""
    
    # 检查环境变量
    if [ -n "$http_proxy" ]; then
        proxy="$http_proxy"
    elif [ -n "$HTTP_PROXY" ]; then
        proxy="$HTTP_PROXY"
    elif [ -n "$https_proxy" ]; then
        proxy="$https_proxy"
    elif [ -n "$HTTPS_PROXY" ]; then
        proxy="$HTTPS_PROXY"
    fi
    
    echo "$proxy"
}

# 解析代理URL
parse_proxy_url() {
    local proxy_url="$1"
    local host=""
    local port=""
    
    # 移除协议前缀
    proxy_url="${proxy_url#http://}"
    proxy_url="${proxy_url#https://}"
    
    # 提取主机和端口
    if [[ "$proxy_url" =~ ^([^:]+):([0-9]+) ]]; then
        host="${BASH_REMATCH[1]}"
        port="${BASH_REMATCH[2]}"
    fi
    
    echo "$host:$port"
}

# 检测代理
PROXY_URL=$(detect_proxy)

if [ -n "$PROXY_URL" ]; then
    echo "检测到系统代理: $PROXY_URL"
    
    # 解析代理
    PROXY_INFO=$(parse_proxy_url "$PROXY_URL")
    IFS=':' read -r PROXY_HOST PROXY_PORT <<< "$PROXY_INFO"
    
    if [ -n "$PROXY_HOST" ] && [ -n "$PROXY_PORT" ]; then
        echo "代理服务器: $PROXY_HOST"
        echo "代理端口: $PROXY_PORT"
        echo ""
        
        # 备份原文件
        cp "$GRADLE_PROPERTIES" "${GRADLE_PROPERTIES}.bak"
        
        # 更新gradle.properties
        echo "正在更新Gradle代理配置..."
        
        # 移除旧的代理配置
        sed -i '/^systemProp\.http\.proxyHost=/d' "$GRADLE_PROPERTIES"
        sed -i '/^systemProp\.http\.proxyPort=/d' "$GRADLE_PROPERTIES"
        sed -i '/^systemProp\.https\.proxyHost=/d' "$GRADLE_PROPERTIES"
        sed -i '/^systemProp\.https\.proxyPort=/d' "$GRADLE_PROPERTIES"
        sed -i '/^systemProp\.http\.nonProxyHosts=/d' "$GRADLE_PROPERTIES"
        
        # 添加新的代理配置
        cat >> "$GRADLE_PROPERTIES" << EOF

# 自动配置的代理设置 ($(date))
systemProp.http.proxyHost=$PROXY_HOST
systemProp.http.proxyPort=$PROXY_PORT
systemProp.https.proxyHost=$PROXY_HOST
systemProp.https.proxyPort=$PROXY_PORT
systemProp.http.nonProxyHosts=localhost|127.0.0.1|*.aliyun.com|*.tencent.com|*.huaweicloud.com
EOF
        
        echo "✅ 代理配置完成"
        
        # 测试代理连接
        echo ""
        echo "测试代理连接..."
        if curl -x "$PROXY_HOST:$PROXY_PORT" -I -m 5 https://www.google.com > /dev/null 2>&1; then
            echo "✅ 代理连接正常"
        else
            echo "⚠️  代理连接测试失败，请检查代理设置"
        fi
    else
        echo "⚠️  无法解析代理地址"
    fi
else
    echo "未检测到系统代理"
    echo ""
    echo "如果您使用代理，请设置环境变量："
    echo "export http_proxy=http://代理地址:端口"
    echo "export https_proxy=http://代理地址:端口"
fi

echo ""
echo "当前Gradle代理配置："
grep -E "systemProp\.(http|https)\.proxy" "$GRADLE_PROPERTIES" || echo "未配置代理"