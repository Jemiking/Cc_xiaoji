@echo off
echo 开始测试记账模块编译...
echo 项目路径: %cd%

echo.
echo === 1. 清理构建缓存 ===
call "%~dp0..\\gradlew.bat" clean --console=plain

echo.
echo === 2. 编译记账模块 ===
call "%~dp0..\\gradlew.bat" :feature:ledger:compileDebugKotlin --console=plain --stacktrace

echo.
echo === 3. 检查编译结果 ===
if %ERRORLEVEL% EQU 0 (
    echo ✅ 记账模块编译成功！
) else (
    echo ❌ 记账模块编译失败，错误代码: %ERRORLEVEL%
)

echo.
echo === 4. 尝试构建整个记账模块 ===
call "%~dp0..\\gradlew.bat" :feature:ledger:assembleDebug --console=plain

if %ERRORLEVEL% EQU 0 (
    echo ✅ 记账模块构建成功！
) else (
    echo ❌ 记账模块构建失败，错误代码: %ERRORLEVEL%
)

echo.
echo 测试完成。
pause
