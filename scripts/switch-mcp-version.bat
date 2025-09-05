@echo off
:: MCP Android Compiler版本切换脚本
:: 用于在原始版本和优化版本之间切换

echo ========================================
echo MCP Android Compiler 版本切换工具
echo ========================================
echo.

set MCP_DIR=D:\kotlin\Cc_xiaoji\android-compiler-mcp-windows
set ORIGINAL_FILE=%MCP_DIR%\index.js
set OPTIMIZED_FILE=%MCP_DIR%\index-optimized.js
set BACKUP_FILE=%MCP_DIR%\index-original.js

:: 检查目录是否存在
if not exist "%MCP_DIR%" (
    echo 错误：MCP目录不存在：%MCP_DIR%
    exit /b 1
)

:: 显示当前版本信息
echo 当前版本检测：
findstr /C:"version: '1.0.0'" "%ORIGINAL_FILE%" >nul 2>&1
if %errorlevel% equ 0 (
    echo - 当前使用：原始版本 (v1.0.0)
    set CURRENT_VERSION=original
) else (
    echo - 当前使用：优化版本 (v2.0.0)
    set CURRENT_VERSION=optimized
)
echo.

:: 显示选项
echo 请选择操作：
echo 1. 切换到原始版本 (v1.0.0) - 兼容性优先
echo 2. 切换到优化版本 (v2.0.0) - 功能增强
echo 3. 查看版本差异
echo 4. 退出
echo.

set /p choice=请输入选择 (1-4): 

if "%choice%"=="1" goto switch_to_original
if "%choice%"=="2" goto switch_to_optimized
if "%choice%"=="3" goto show_diff
if "%choice%"=="4" goto exit

echo 无效的选择！
goto exit

:switch_to_original
echo.
echo 切换到原始版本...

:: 如果备份文件存在，使用备份
if exist "%BACKUP_FILE%" (
    copy /Y "%BACKUP_FILE%" "%ORIGINAL_FILE%" >nul
    echo ✓ 已恢复到原始版本
) else (
    echo 警告：找不到原始版本备份文件
    echo 请确保 index-original.js 存在
)
goto success

:switch_to_optimized
echo.
echo 切换到优化版本...

:: 首先备份原始版本
if not exist "%BACKUP_FILE%" (
    if exist "%ORIGINAL_FILE%" (
        copy /Y "%ORIGINAL_FILE%" "%BACKUP_FILE%" >nul
        echo ✓ 已备份原始版本
    )
)

:: 切换到优化版本
if exist "%OPTIMIZED_FILE%" (
    copy /Y "%OPTIMIZED_FILE%" "%ORIGINAL_FILE%" >nul
    echo ✓ 已切换到优化版本
) else (
    echo 错误：找不到优化版本文件 (index-optimized.js)
    exit /b 1
)
goto success

:show_diff
echo.
echo 版本差异说明：
echo.
echo 【原始版本 v1.0.0】
echo - 最大兼容性，跳过所有Android资源处理任务
echo - 编译速度快，但可能导致测试编译失败
echo - 适用于：纯Kotlin代码编译
echo.
echo 【优化版本 v2.0.0】
echo - 智能判断，保留必要的构建步骤
echo - 新增 prepare_android_build 工具
echo - 支持预构建步骤 (preBuild 参数)
echo - 改进的错误诊断和建议
echo - 适用于：完整的Android项目编译，包括测试
echo.
pause
goto exit

:success
echo.
echo ========================================
echo ✓ 版本切换成功！
echo ========================================
echo.
echo 注意：需要重启Claude Code才能生效
echo.
echo 测试新配置：
echo 1. 关闭Claude Code
echo 2. 重新打开Claude Code
echo 3. 使用 mcp__android-compiler__check_gradle 测试
echo.

:exit
pause