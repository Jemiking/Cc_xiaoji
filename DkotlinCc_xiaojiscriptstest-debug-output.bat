#\!/bin/bash
echo "开始验证调试输出..."

# 清理并重新编译
echo "1. 清理项目..."
D:\kotlin\Cc_xiaoji\gradlew.bat clean

echo "2. 编译Debug版本..."
D:\kotlin\Cc_xiaoji\gradlew.bat :app:assembleDebug

echo "3. 安装到设备..."
D:\kotlin\Cc_xiaoji\gradlew.bat :app:installDebug

echo "4. 清空Logcat..."
adb logcat -c

echo "5. 启动应用..."
adb shell am start -n com.ccxiaoji.app/.presentation.MainActivity

echo "6. 等待2秒..."
timeout /t 2 /nobreak > nul

echo "7. 查看日志输出..."
adb logcat -d | findstr /I "CC_DEBUG QIANJI System.out Toast"

echo "完成！"

