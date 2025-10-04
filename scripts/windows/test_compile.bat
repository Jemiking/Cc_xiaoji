@echo off
echo 测试编译ledger模块...
call "%~dp0..\\gradlew.bat" :feature:ledger:compileDebugKotlin --console=plain
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ===================================
    echo 编译成功！
    echo ===================================
) else (
    echo.
    echo ===================================
    echo 编译失败，请检查错误信息
    echo ===================================
)
pause
