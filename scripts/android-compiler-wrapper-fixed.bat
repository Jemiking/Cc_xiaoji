@echo off
REM Android Compiler MCP Wrapper - 完整修复版
REM 确保正确的stdio传递和MCP握手

REM 切换到脚本所在目录（解决工作目录问题）
cd /d %~dp0

REM 设置环境变量
set "NODE_ENV=development"

REM 直接使用wsl --exec来保持stdio连接
REM 使用bash -l -i来确保交互式shell和完整环境
wsl -d Ubuntu --exec bash -l -i -c "cd /home/ua/android-compiler-mcp && source ~/.nvm/nvm.sh && exec node index.js"