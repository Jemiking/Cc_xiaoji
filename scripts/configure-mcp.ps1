# MCP配置脚本
Write-Host "MCP服务器配置脚本" -ForegroundColor Green
Write-Host "==================" -ForegroundColor Green

# 检查Git是否安装
try {
    $gitPath = Get-Command git -ErrorAction Stop
    Write-Host "✓ Git已安装: $($gitPath.Source)" -ForegroundColor Green
} catch {
    Write-Host "✗ 错误：未找到Git。请从 https://git-scm.com/downloads/win 安装Git for Windows" -ForegroundColor Red
    exit 1
}

# 定义函数执行Git Bash命令
function Invoke-GitBash {
    param([string]$Command)
    $result = & git bash -c $Command 2>&1
    return $result
}

Write-Host "`n步骤1：检查当前MCP配置" -ForegroundColor Yellow
Write-Host "=========================" -ForegroundColor Yellow
$currentConfig = Invoke-GitBash "claude mcp list"
Write-Host $currentConfig

Write-Host "`n步骤2：清理旧配置" -ForegroundColor Yellow
Write-Host "==================" -ForegroundColor Yellow
Invoke-GitBash "claude mcp remove o3mcp 2>/dev/null || true" | Out-Null
Invoke-GitBash "claude mcp remove android-compiler 2>/dev/null || true" | Out-Null
Write-Host "✓ 旧配置已清理" -ForegroundColor Green

Write-Host "`n步骤3：添加o3mcp服务器" -ForegroundColor Yellow
Write-Host "=======================" -ForegroundColor Yellow
$o3mcpCommand = @"
claude mcp add o3mcp -s user \
  -e OPENAI_API_KEY=sk-AjLiv6wVrbxtroActCAuqeirwwBhgLx1dBy6VQaLL8hnHAGgB2ET \
  -e OPENAI_BASE_URL=https://api.oaipro.com/v1 \
  -e O3_MODEL=o3-2025-04-16 \
  -e NODE_ENV=development \
  -e LOG_LEVEL=debug \
  -- node 'D:/开发项目/mcp/o3mcp/dist/index.js'
"@
$result = Invoke-GitBash $o3mcpCommand.Replace("`n", "").Replace("\", "")
Write-Host $result
Write-Host "✓ o3mcp已添加" -ForegroundColor Green

Write-Host "`n步骤4：添加android-compiler服务器" -ForegroundColor Yellow
Write-Host "===================================" -ForegroundColor Yellow
$androidCommand = @"
claude mcp add android-compiler -s user \
  -e NODE_ENV=development \
  -- wsl node /home/hua/android-compiler-mcp/index.js
"@
$result = Invoke-GitBash $androidCommand.Replace("`n", "").Replace("\", "")
Write-Host $result
Write-Host "✓ android-compiler已添加" -ForegroundColor Green

Write-Host "`n步骤5：验证最终配置" -ForegroundColor Yellow
Write-Host "====================" -ForegroundColor Yellow
$finalConfig = Invoke-GitBash "claude mcp list"
Write-Host $finalConfig

Write-Host "`n✓ 配置完成！" -ForegroundColor Green
Write-Host "请执行以下步骤：" -ForegroundColor Cyan
Write-Host "1. 输入 'exit' 退出当前Claude会话" -ForegroundColor Cyan
Write-Host "2. 重新运行 'claude' 启动新会话" -ForegroundColor Cyan
Write-Host "3. 在新会话中输入 '/mcp' 验证MCP服务器是否加载" -ForegroundColor Cyan