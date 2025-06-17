@echo off
echo === 编译并测试分类系统修复 ===
echo.

echo 1. 编译项目...
call gradlew.bat :app:compileDebugKotlin
if %errorlevel% neq 0 (
    echo    × 编译失败
    pause
    exit /b 1
)
echo    √ 编译成功

echo.
echo 2. 构建 APK...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo    × 构建失败
    pause
    exit /b 1
)
echo    √ APK 构建成功

echo.
echo 修复完成！现在可以：
echo 1. 运行 debug_database.bat 进行完整测试
echo 2. 或手动安装 APK 测试
echo.
pause