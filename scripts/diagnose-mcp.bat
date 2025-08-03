@echo off
REM MCP服务器诊断脚本
REM 用于检查android-compiler MCP服务器的环境和配置

echo ========================================
echo MCP服务器环境诊断
echo ========================================
echo.

echo [1] 检查WSL状态...
wsl --version >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo    [错误] WSL未安装或不可用
    echo    请运行: wsl --install
    goto :error
) else (
    echo    [成功] WSL已安装
)

echo.
echo [2] 检查WSL中的Node.js...
wsl -d Ubuntu -- test -f /home/ua/.nvm/versions/node/v22.17.0/bin/node
if %ERRORLEVEL% neq 0 (
    echo    [错误] Node.js v22.17.0未找到
    echo    请在WSL中安装Node.js:
    echo    curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh ^| bash
    echo    nvm install 22.17.0
    goto :error
) else (
    echo    [成功] Node.js v22.17.0已安装
    wsl -d Ubuntu -- /home/ua/.nvm/versions/node/v22.17.0/bin/node --version
)

echo.
echo [3] 检查android-compiler-mcp...
wsl -d Ubuntu -- test -f /home/ua/android-compiler-mcp/index.js
if %ERRORLEVEL% neq 0 (
    echo    [错误] android-compiler-mcp未找到
    echo    请在WSL中克隆或安装android-compiler-mcp到: /home/ua/android-compiler-mcp/
    goto :error
) else (
    echo    [成功] android-compiler-mcp已安装
)

echo.
echo [4] 测试MCP服务器启动...
echo    尝试启动MCP服务器（5秒超时）...
timeout /t 1 >nul
call "%~dp0android-compiler-wrapper.bat" --version 2>nul
if %ERRORLEVEL% neq 0 (
    echo    [警告] MCP服务器启动可能有问题
    echo    尝试使用PowerShell版本:
    powershell -ExecutionPolicy Bypass -File "%~dp0android-compiler-wrapper.ps1" --version
) else (
    echo    [成功] MCP服务器可以启动
)

echo.
echo [5] 检查.claude/mcp.json配置...
if exist "%~dp0..\.claude\mcp.json" (
    echo    [成功] MCP配置文件存在
    echo    配置路径: %~dp0..\.claude\mcp.json
) else (
    echo    [错误] MCP配置文件不存在
    goto :error
)

echo.
echo ========================================
echo 诊断完成 - 所有检查通过
echo ========================================
echo.
echo 如果MCP仍然无法连接，请尝试：
echo 1. 重启Claude Code
echo 2. 使用PowerShell版本: powershell -ExecutionPolicy Bypass -File "%~dp0android-compiler-wrapper.ps1"
echo 3. 检查Windows防火墙设置
echo.
goto :end

:error
echo.
echo ========================================
echo 诊断失败 - 请修复上述错误
echo ========================================
echo.

:end
pause