@echo off
echo === CC小记数据库调试工具 (Windows版) ===
echo.

rem 检查设备连接
adb devices > temp_devices.txt 2>&1
findstr /C:"device" temp_devices.txt > nul
if errorlevel 1 (
    echo 错误: 没有检测到连接的设备或模拟器
    echo 请确保:
    echo 1. 设备已连接并启用 USB 调试
    echo 2. 模拟器已启动
    del temp_devices.txt
    pause
    exit /b 1
)
del temp_devices.txt

set PACKAGE_NAME=com.ccxiaoji.app

echo 1. 清理应用数据...
adb shell pm clear %PACKAGE_NAME% > nul 2>&1
if %errorlevel% equ 0 (
    echo    √ 应用数据已清理
) else (
    echo    × 清理失败，请检查包名是否正确
    pause
    exit /b 1
)

echo.
echo 2. 重新编译应用...
echo    正在构建 Debug APK...
call gradlew.bat assembleDebug
if %errorlevel% equ 0 (
    echo    √ APK 构建成功
) else (
    echo    × 构建失败
    pause
    exit /b 1
)

echo.
echo 3. 安装应用...
adb install -r app\build\outputs\apk\debug\app-debug.apk > nul 2>&1
if %errorlevel% equ 0 (
    echo    √ 应用安装成功
) else (
    echo    × 安装失败
    pause
    exit /b 1
)

echo.
echo 4. 启动应用...
adb shell am start -n %PACKAGE_NAME%/.presentation.MainActivity > nul 2>&1
if %errorlevel% equ 0 (
    echo    √ 应用已启动
) else (
    echo    × 启动失败
)

echo.
echo 5. 查看日志...
echo    按 Ctrl+C 停止查看日志
echo    ================================
echo.

rem 清理旧日志并开始监控新日志
adb logcat -c
adb logcat -v time CcXiaoJi:D DatabaseModule:D DatabaseInitializer:D AndroidRuntime:E *:S