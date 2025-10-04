@echo off
REM MCP Configuration Diagnostics

echo ========================================
echo MCP Configuration Diagnostics
echo ========================================
echo.

echo [1] Checking Claude installation...
where claude >nul 2>nul
if %errorlevel% equ 0 (
    echo OK - Claude is installed
    claude --version
) else (
    echo ERROR - Claude not found, please ensure it's installed and in PATH
)

echo.
echo [2] Checking Node.js...
where node >nul 2>nul
if %errorlevel% equ 0 (
    echo OK - Node.js is installed
    node --version
) else (
    echo ERROR - Node.js not found, please install Node.js
)

echo.
echo [3] Checking MCP project files...
if exist "D:\kotlin\Cc_xiaoji\android-compiler-mcp-windows\index.js" (
    echo OK - MCP project files exist
) else (
    echo ERROR - MCP project files not found
)

echo.
echo [4] Current MCP configuration...
claude mcp list 2>nul

echo.
echo [5] Testing MCP server...
echo {"jsonrpc":"2.0","method":"tools/list","id":1} | node "D:\kotlin\Cc_xiaoji\android-compiler-mcp-windows\index.js" 2>nul
if %errorlevel% equ 0 (
    echo.
    echo OK - MCP server is working
) else (
    echo.
    echo ERROR - MCP server test failed
)

echo.
echo ========================================
pause

