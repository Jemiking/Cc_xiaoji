#!/bin/bash

# 数据库调试脚本
# 用于清理应用数据并重新测试数据库初始化

echo "=== CC小记数据库调试工具 ==="
echo

# 获取设备列表
devices=$(adb devices | grep -E "device$" | cut -f1)
device_count=$(echo "$devices" | wc -l)

if [ -z "$devices" ]; then
    echo "错误: 没有检测到连接的设备或模拟器"
    echo "请确保:"
    echo "1. 设备已连接并启用 USB 调试"
    echo "2. 模拟器已启动"
    exit 1
fi

# 如果有多个设备，让用户选择
if [ $device_count -gt 1 ]; then
    echo "检测到多个设备:"
    echo "$devices"
    echo -n "请输入要使用的设备ID: "
    read selected_device
    DEVICE_FLAG="-s $selected_device"
else
    DEVICE_FLAG=""
fi

PACKAGE_NAME="com.ccxiaoji.app"

echo "1. 清理应用数据..."
adb $DEVICE_FLAG shell pm clear $PACKAGE_NAME
if [ $? -eq 0 ]; then
    echo "   ✓ 应用数据已清理"
else
    echo "   ✗ 清理失败，请检查包名是否正确"
    exit 1
fi

echo
echo "2. 重新安装应用..."
echo "   正在构建 Debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "   ✓ APK 构建成功"
    echo "   正在安装..."
    adb $DEVICE_FLAG install -r app/build/outputs/apk/debug/app-debug.apk
    if [ $? -eq 0 ]; then
        echo "   ✓ 应用安装成功"
    else
        echo "   ✗ 安装失败"
        exit 1
    fi
else
    echo "   ✗ 构建失败"
    exit 1
fi

echo
echo "3. 启动应用..."
adb $DEVICE_FLAG shell am start -n $PACKAGE_NAME/.presentation.MainActivity
if [ $? -eq 0 ]; then
    echo "   ✓ 应用已启动"
else
    echo "   ✗ 启动失败"
fi

echo
echo "4. 查看日志..."
echo "   按 Ctrl+C 停止查看日志"
echo "   ================================"
echo

# 清理旧日志并开始监控新日志
adb $DEVICE_FLAG logcat -c
adb $DEVICE_FLAG logcat -v time CcXiaoJi:D DatabaseModule:D DatabaseInitializer:D AndroidRuntime:E *:S