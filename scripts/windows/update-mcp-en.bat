@echo off
REM Update Claude Code MCP configuration to Windows native

echo ========================================
echo Update Claude Code MCP Configuration
echo ========================================
echo.

echo [1] Removing old WSL configuration...
claude mcp remove android-compiler -s user 2>nul
claude mcp remove android-compiler -s project 2>nul
claude mcp remove android-compiler -s local 2>nul

echo.
echo [2] Adding new Windows native configuration...
cd /d D:\kotlin\Cc_xiaoji
claude mcp add android-compiler -s user -- node "D:/kotlin/Cc_xiaoji/android-compiler-mcp-windows/index.js"

echo.
echo [3] Listing current MCP configuration...
claude mcp list

echo.
echo ========================================
echo Configuration update complete!
echo.
echo Please follow these steps:
echo 1. Exit current Claude Code session (type exit)
echo 2. Restart Claude Code
echo 3. MCP server should now run natively on Windows
echo ========================================
pause

