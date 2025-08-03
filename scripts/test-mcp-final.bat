@echo off
REM 最终MCP测试脚本

echo ========================================
echo MCP服务器最终测试
echo ========================================
echo.

echo [1] 测试Node.js访问...
wsl -d Ubuntu -- bash -l -c "source ~/.nvm/nvm.sh && /home/ua/.nvm/versions/node/v22.17.0/bin/node --version"
if %ERRORLEVEL% neq 0 (
    echo    [错误] Node.js无法访问
    goto :error
) else (
    echo    [成功] Node.js可以访问
)

echo.
echo [2] 测试android-compiler-mcp存在性...
wsl -d Ubuntu -- test -f /home/ua/android-compiler-mcp/index.js
if %ERRORLEVEL% neq 0 (
    echo    [错误] android-compiler-mcp不存在
    goto :error
) else (
    echo    [成功] android-compiler-mcp存在
)

echo.
echo [3] 测试wrapper脚本...
call "%~dp0android-compiler-wrapper-fixed.bat" --version
if %ERRORLEVEL% neq 0 (
    echo    [警告] wrapper脚本可能有问题
) else (
    echo    [成功] wrapper脚本正常
)

echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
echo MCP配置已更新为：
echo - 命令: D:\kotlin\Cc_xiaoji\scripts\android-compiler-wrapper-fixed.bat
echo.
echo 请重启Claude Code以应用新配置！
echo.
goto :end

:error
echo.
echo ========================================
echo 测试失败 - 请检查错误信息
echo ========================================
echo.

:end
pause