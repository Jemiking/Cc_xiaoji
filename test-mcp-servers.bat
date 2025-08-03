@echo off
echo ========================================
echo Testing MCP Servers
echo ========================================
echo.

echo [1] Testing android-compiler MCP...
echo {"jsonrpc":"2.0","method":"tools/list","id":1} | node "D:\kotlin\Cc_xiaoji\android-compiler-mcp-windows\index.js" > android-compiler-test.json 2>&1
if %errorlevel% equ 0 (
    echo OK - android-compiler responded
    type android-compiler-test.json | findstr /i "compile_kotlin"
) else (
    echo ERROR - android-compiler failed
    type android-compiler-test.json
)

echo.
echo [2] Testing o3mcp MCP...
echo {"jsonrpc":"2.0","method":"tools/list","id":1} | node "D:\开发项目\mcp\o3mcp\dist\index.js" > o3mcp-test.json 2>&1
if %errorlevel% equ 0 (
    echo OK - o3mcp responded
    type o3mcp-test.json | findstr /i "understand_with_o3"
) else (
    echo ERROR - o3mcp failed
    type o3mcp-test.json
)

echo.
echo [3] Cleaning up test files...
del android-compiler-test.json 2>nul
del o3mcp-test.json 2>nul

echo.
echo ========================================
echo Test completed
echo.
echo IMPORTANT: After running this test, you need to:
echo 1. Type "exit" in Claude Code to quit
echo 2. Start Claude Code again with "claude"
echo 3. The MCP servers should now be working
echo ========================================
pause