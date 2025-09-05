@echo off
echo 验证MCP设置...
echo.

echo 1. 启动WSL Ubuntu:
wsl -d Ubuntu -- true
if %ERRORLEVEL% equ 0 (
    echo    [OK] WSL Ubuntu可用
) else (
    echo    [ERROR] WSL Ubuntu不可用
    wsl --list --verbose
    exit /b 1
)

echo.
echo 2. 检查Node.js:
wsl -d Ubuntu -- bash -l -c "source ~/.nvm/nvm.sh && node --version"
echo.

echo 3. 检查android-compiler-mcp:
wsl -d Ubuntu -- ls -la /home/ua/android-compiler-mcp/index.js
echo.

echo 4. 测试wrapper脚本:
call "%~dp0android-compiler-wrapper-fixed.bat"
echo.

echo 配置总结:
echo - MCP配置文件: .claude\mcp.json
echo - Wrapper脚本: scripts\android-compiler-wrapper-fixed.bat
echo - WSL路径: /home/ua/android-compiler-mcp/index.js
echo.
echo 如果以上检查都通过，请重启Claude Code！
pause