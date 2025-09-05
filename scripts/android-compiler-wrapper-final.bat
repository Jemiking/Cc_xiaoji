@echo off
REM Android Compiler MCP Wrapper - 最终修复版
REM 确保MCP协议握手正确进行

REM 设置环境变量
set "NODE_ENV=production"

REM 使用--exec确保stdio正确连接，并将stderr重定向到nul以避免干扰
wsl -d Ubuntu --exec bash -l -c "cd /home/ua/android-compiler-mcp && source ~/.nvm/nvm.sh && exec node index.js 2>>/tmp/mcp-android-compiler.log"