#!/bin/bash

echo "===== CC小记调试脚本 ====="
echo "1. 清理项目..."
./gradlew clean

echo ""
echo "2. 重新编译项目..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo ""
    echo "3. 编译成功！现在请安装并运行应用。"
    echo ""
    echo "4. 使用以下命令查看调试日志："
    echo "   adb logcat -c  # 清除旧日志"
    echo "   adb logcat | grep -E 'CcXiaoJi|AndroidRuntime'"
    echo ""
    echo "5. 应用崩溃后，查找以下关键信息："
    echo "   - Application onCreate started/completed"
    echo "   - MainActivity onCreate started/completed"
    echo "   - Database initialization logs"
    echo "   - HomeViewModel init logs"
    echo "   - AndroidRuntime: FATAL EXCEPTION"
    echo ""
    echo "6. 请将包含 'AndroidRuntime: FATAL EXCEPTION' 的完整错误栈发送给我。"
else
    echo ""
    echo "编译失败！请检查错误信息。"
fi