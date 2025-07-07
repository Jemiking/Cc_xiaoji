#!/bin/bash

echo "=== 测试网络连接 ==="
echo ""

# 测试各个下载源
echo "1. 测试Gradle官方源："
curl -I -m 10 https://services.gradle.org/distributions/gradle-8.5-bin.zip 2>&1 | head -n 1
echo ""

echo "2. 测试腾讯云镜像："
curl -I -m 10 https://mirrors.cloud.tencent.com/gradle/gradle-8.5-bin.zip 2>&1 | head -n 1
echo ""

echo "3. 测试阿里云Maven："
curl -I -m 10 https://maven.aliyun.com/repository/public 2>&1 | head -n 1
echo ""

echo "4. 检查代理设置："
echo "HTTP_PROXY: ${HTTP_PROXY:-未设置}"
echo "HTTPS_PROXY: ${HTTPS_PROXY:-未设置}"
echo "http_proxy: ${http_proxy:-未设置}"
echo "https_proxy: ${https_proxy:-未设置}"
echo ""

echo "5. 检查DNS解析："
nslookup services.gradle.org | grep -A 1 "Name:" || echo "DNS解析失败"
echo ""

echo "6. 检查WSL网络模式："
if [ -f /etc/resolv.conf ]; then
    echo "DNS服务器："
    cat /etc/resolv.conf | grep nameserver
fi