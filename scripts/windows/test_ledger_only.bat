@echo off
echo 专门测试记账模块编译状态...

echo.
echo === 测试记账模块独立编译 ===
call "%~dp0..\\gradlew.bat" :feature:ledger:build --console=plain

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ✅ 记账模块编译测试：成功
    echo.
    echo === 模块编译任务详情 ===
    call "%~dp0..\\gradlew.bat" :feature:ledger:tasks --group="build" --console=plain
) else (
    echo.
    echo ❌ 记账模块编译测试：失败，错误代码: %ERRORLEVEL%
)

echo.
echo 测试完成。
