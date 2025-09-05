@echo off
echo ========================================
echo 测试调试输出
echo ========================================

cd /d D:\kotlin\Cc_xiaoji

echo.
echo 1. 清理项目...
call gradlew.bat clean
if errorlevel 1 (
    echo 清理失败！
    pause
    exit /b 1
)

echo.
echo 2. 编译Debug版本...
call gradlew.bat :app:assembleDebug
if errorlevel 1 (
    echo 编译失败！
    pause
    exit /b 1
)

echo.
echo 3. 安装到设备...
call gradlew.bat :app:installDebug
if errorlevel 1 (
    echo 安装失败！请确保设备已连接并启用调试模式
    pause
    exit /b 1
)

echo.
echo 4. 检查adb是否可用...
where adb >nul 2>nul
if errorlevel 1 (
    echo adb不在系统PATH中，尝试使用Android SDK路径...
    set ADB_PATH=C:\Users\%USERNAME%\AppData\Local\Android\Sdk\platform-tools\adb.exe
    if not exist "!ADB_PATH!" (
        echo 找不到adb，请确保Android SDK已安装
        pause
        exit /b 1
    )
) else (
    set ADB_PATH=adb
)

echo 使用ADB: %ADB_PATH%

echo.
echo 5. 清空Logcat...
%ADB_PATH% logcat -c

echo.
echo 6. 启动应用...
%ADB_PATH% shell am start -n com.ccxiaoji.app/.presentation.MainActivity

echo.
echo 7. 等待3秒...
ping 127.0.0.1 -n 4 > nul

echo.
echo 8. 查看所有日志（最近100条）...
echo ----------------------------------------
%ADB_PATH% logcat -d -t 100

echo.
echo 9. 查看CC_DEBUG标签日志...
echo ----------------------------------------
%ADB_PATH% logcat -d -s CC_DEBUG:*

echo.
echo 10. 查看System.out输出...
echo ----------------------------------------
%ADB_PATH% logcat -d -s System.out:*

echo.
echo 11. 查看MainActivity相关日志...
echo ----------------------------------------
%ADB_PATH% logcat -d | findstr MainActivity

echo.
echo 12. 查看应用进程是否在运行...
echo ----------------------------------------
%ADB_PATH% shell ps | findstr ccxiaoji

echo.
echo 完成！
pause