#!/bin/bash
# 开发环境数据库清理脚本
# 仅在开发阶段使用！

echo "清理开发环境数据库..."
echo "警告：这将删除应用的所有数据！"
read -p "确定要继续吗？(y/n) " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]
then
    # 清理应用数据
    adb shell pm clear com.ccxiaoji.app
    
    if [ $? -eq 0 ]; then
        echo "✅ 数据库已清理"
        echo "下次启动应用时将创建全新的版本1数据库"
    else
        echo "❌ 清理失败，请确保："
        echo "1. 设备已连接"
        echo "2. ADB 已正确配置"
        echo "3. 应用已安装"
    fi
else
    echo "已取消"
fi