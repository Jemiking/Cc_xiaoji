@echo off
echo === 测试编译app模块 ===
echo.

REM 清理之前的编译输出
if exist app_compile_errors.txt del app_compile_errors.txt

echo 开始编译app模块...
echo.

REM 编译app模块并捕获错误
call "%~dp0..\\gradlew.bat" :app:compileDebugKotlin --no-daemon --console=plain 2>app_compile_errors.txt

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo 编译失败！错误已保存到 app_compile_errors.txt
    echo.
    echo 显示前50个错误：
    echo ==================
    powershell -Command "Get-Content app_compile_errors.txt | Select-Object -First 50"
) else (
    echo.
    echo 编译成功！
)

echo.
echo 统计错误数量...
powershell -Command "(Get-Content app_compile_errors.txt | Select-String 'e: file:').Count"
echo 个编译错误
