@echo off
chcp 65001 >nul
echo =============================================
echo 📱 CC小记 APK安装工具
echo =============================================
echo.
echo 选择要安装的版本：
echo.
echo 1. 🐛 Debug版本 (com.ccxiaoji.app.debug)
echo    - 包含调试信息，便于问题排查
echo    - 可与正式版同时安装
echo.
echo 2. 🚀 Release版本 (com.ccxiaoji.app)  
echo    - 正式签名版本，用于发布
echo    - 代码已优化和混淆
echo.
echo 3. 🗑️ 卸载Debug版本
echo.
echo 4. 🗑️ 卸载Release版本
echo.
echo 5. 🗑️ 卸载所有版本
echo.
echo 6. 📋 查看已安装版本
echo.
echo 0. ❌ 退出
echo.

set /p choice=请输入选择(0-6): 

if "%choice%"=="1" (
    echo.
    echo 正在安装Debug版本...
    echo 📁 APK路径: app\build\outputs\apk\debug\app-debug.apk
    if exist "app\build\outputs\apk\debug\app-debug.apk" (
        adb install -r "app\build\outputs\apk\debug\app-debug.apk"
        if %errorlevel% equ 0 (
            echo ✅ Debug版本安装成功！
            echo 📱 包名: com.ccxiaoji.app.debug
        ) else (
            echo ❌ 安装失败，请检查设备连接和USB调试设置
        )
    ) else (
        echo ❌ 未找到Debug版APK，请先构建：
        echo    • Android Studio: Build Variants → debug → Ctrl+F9
        echo    • 命令行: build_debug.bat
    )
) else if "%choice%"=="2" (
    echo.
    echo 正在安装Release版本...
    echo 📁 APK路径: app\build\outputs\apk\release\app-release.apk
    if exist "app\build\outputs\apk\release\app-release.apk" (
        adb install -r "app\build\outputs\apk\release\app-release.apk"
        if %errorlevel% equ 0 (
            echo ✅ Release版本安装成功！
            echo 📱 包名: com.ccxiaoji.app
        ) else (
            echo ❌ 安装失败，请检查设备连接和USB调试设置
        )
    ) else (
        echo ❌ 未找到Release版APK，请先构建：
        echo    • Android Studio: Build Variants → release → Ctrl+F9  
        echo    • 命令行: build_release.bat
    )
) else if "%choice%"=="3" (
    echo.
    echo 正在卸载Debug版本...
    adb uninstall com.ccxiaoji.app.debug
    if %errorlevel% equ 0 (
        echo ✅ Debug版本卸载成功！
    ) else (
        echo ⚠️ 卸载失败或应用未安装
    )
) else if "%choice%"=="4" (
    echo.
    echo 正在卸载Release版本...
    adb uninstall com.ccxiaoji.app
    if %errorlevel% equ 0 (
        echo ✅ Release版本卸载成功！
    ) else (
        echo ⚠️ 卸载失败或应用未安装
    )
) else if "%choice%"=="5" (
    echo.
    echo 正在卸载所有版本...
    echo 🗑️ 卸载Debug版本...
    adb uninstall com.ccxiaoji.app.debug
    echo 🗑️ 卸载Release版本...
    adb uninstall com.ccxiaoji.app
    echo ✅ 卸载完成！
) else if "%choice%"=="6" (
    echo.
    echo 📋 检查已安装的CC小记版本...
    echo.
    echo Debug版本状态:
    adb shell pm list packages | findstr "com.ccxiaoji.app.debug" >nul
    if %errorlevel% equ 0 (
        echo ✅ Debug版本已安装 (com.ccxiaoji.app.debug)
    ) else (
        echo ❌ Debug版本未安装
    )
    echo.
    echo Release版本状态:
    adb shell pm list packages | findstr "com.ccxiaoji.app$" >nul
    if %errorlevel% equ 0 (
        echo ✅ Release版本已安装 (com.ccxiaoji.app)
    ) else (
        echo ❌ Release版本未安装
    )
) else if "%choice%"=="0" (
    echo 👋 再见！
    exit /b 0
) else (
    echo ❌ 无效选择，请重新运行脚本
)

echo.
pause