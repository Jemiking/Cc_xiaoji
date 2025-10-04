@echo off
echo ========================================
echo 清理项目缓存和重新编译
echo ========================================

echo.
echo [1/4] 删除构建目录...
if exist "%~dp0..\build" rmdir /s /q "%~dp0..\build"
if exist "%~dp0..\feature\ledger\build" rmdir /s /q "%~dp0..\feature\ledger\build"
if exist "%~dp0..\.gradle" rmdir /s /q "%~dp0..\.gradle"
echo 构建目录已删除

echo.
echo [2/4] 清理整个项目...
call "%~dp0..\gradlew.bat" clean
if %errorlevel% neq 0 (
    echo 清理项目失败！
    echo 尝试手动删除...
)

echo.
echo [3/4] 清理构建缓存...
call "%~dp0..\gradlew.bat" cleanBuildCache
if %errorlevel% neq 0 (
    echo 清理缓存失败（可能不支持该任务）
    echo 继续执行...
)

echo.
echo [4/4] 重新编译 feature:ledger 模块...
call "%~dp0..\gradlew.bat" :feature:ledger:compileDebugKotlin --console=plain
if %errorlevel% neq 0 (
    echo 编译失败！
    exit /b %errorlevel%
)

echo.
echo ========================================
echo 清理和编译完成！
echo ========================================
