@echo off
echo 配置MCP服务器...

REM 检查Git Bash是否安装
where git >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo 错误：未找到Git。请从 https://git-scm.com/downloads/win 安装Git for Windows
    exit /b 1
)

echo.
echo 步骤1：检查当前MCP配置
echo ===========================
git bash -c "claude mcp list"

echo.
echo 步骤2：移除可能存在的旧配置（如果有）
echo ===========================
git bash -c "claude mcp remove o3mcp 2>/dev/null || true"
git bash -c "claude mcp remove android-compiler 2>/dev/null || true"

echo.
echo 步骤3：添加o3mcp服务器
echo ===========================
git bash -c "claude mcp add o3mcp -s user -e OPENAI_API_KEY=sk-AjLiv6wVrbxtroActCAuqeirwwBhgLx1dBy6VQaLL8hnHAGgB2ET -e OPENAI_BASE_URL=https://api.oaipro.com/v1 -e O3_MODEL=o3-2025-04-16 -e NODE_ENV=development -e LOG_LEVEL=debug -- node 'D:/开发项目/mcp/o3mcp/dist/index.js'"

echo.
echo 步骤4：添加android-compiler服务器
echo ===========================
git bash -c "claude mcp add android-compiler -s user -e NODE_ENV=development -- wsl node /home/hua/android-compiler-mcp/index.js"

echo.
echo 步骤5：验证配置
echo ===========================
git bash -c "claude mcp list"

echo.
echo 配置完成！请重启Claude Code使配置生效。
echo 提示：使用 "exit" 退出当前Claude会话，然后重新运行 "claude" 启动
pause