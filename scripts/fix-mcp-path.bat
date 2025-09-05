@echo off
echo 修复MCP配置路径...

REM 备份原配置
copy /Y "%USERPROFILE%\.claude\mcp.json" "%USERPROFILE%\.claude\mcp.json.backup" >nul 2>&1

REM 创建修复后的配置
echo { > "%USERPROFILE%\.claude\mcp.json"
echo   "mcpServers": { >> "%USERPROFILE%\.claude\mcp.json"
echo     "o3mcp": { >> "%USERPROFILE%\.claude\mcp.json"
echo       "command": "node", >> "%USERPROFILE%\.claude\mcp.json"
echo       "args": ["D:\\开发项目\\mcp\\o3mcp\\dist\\index.js"], >> "%USERPROFILE%\.claude\mcp.json"
echo       "env": { >> "%USERPROFILE%\.claude\mcp.json"
echo         "OPENAI_API_KEY": "sk-AjLiv6wVrbxtroActCAuqeirwwBhgLx1dBy6VQaLL8hnHAGgB2ET", >> "%USERPROFILE%\.claude\mcp.json"
echo         "OPENAI_BASE_URL": "https://api.oaipro.com/v1", >> "%USERPROFILE%\.claude\mcp.json"
echo         "O3_MODEL": "o3-2025-04-16", >> "%USERPROFILE%\.claude\mcp.json"
echo         "NODE_ENV": "development", >> "%USERPROFILE%\.claude\mcp.json"
echo         "LOG_LEVEL": "debug" >> "%USERPROFILE%\.claude\mcp.json"
echo       } >> "%USERPROFILE%\.claude\mcp.json"
echo     }, >> "%USERPROFILE%\.claude\mcp.json"
echo     "android-compiler": { >> "%USERPROFILE%\.claude\mcp.json"
echo       "command": "wsl", >> "%USERPROFILE%\.claude\mcp.json"
echo       "args": ["/home/hua/.nvm/versions/node/v22.17.0/bin/node", "/home/hua/android-compiler-mcp/index.js"], >> "%USERPROFILE%\.claude\mcp.json"
echo       "env": { >> "%USERPROFILE%\.claude\mcp.json"
echo         "NODE_ENV": "development" >> "%USERPROFILE%\.claude\mcp.json"
echo       } >> "%USERPROFILE%\.claude\mcp.json"
echo     } >> "%USERPROFILE%\.claude\mcp.json"
echo   } >> "%USERPROFILE%\.claude\mcp.json"
echo } >> "%USERPROFILE%\.claude\mcp.json"

echo MCP路径已修复！请重启Claude Code以应用更改。