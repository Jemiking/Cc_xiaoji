# MCP配置说明

## 执行步骤

### 方法1：使用批处理脚本（推荐）
```bash
cd D:\kotlin\Cc_xiaoji\scripts
configure-mcp.bat
```

### 方法2：使用PowerShell脚本
1. 打开PowerShell（管理员权限）
2. 如果遇到执行策略问题，先运行：
   ```powershell
   Set-ExecutionPolicy -Scope CurrentUser -ExecutionPolicy RemoteSigned
   ```
3. 执行脚本：
   ```powershell
   cd D:\kotlin\Cc_xiaoji\scripts
   .\configure-mcp.ps1
   ```

### 方法3：手动在Git Bash中执行
1. 打开Git Bash
2. 依次执行以下命令：

```bash
# 检查当前配置
claude mcp list

# 移除旧配置（如果有）
claude mcp remove o3mcp
claude mcp remove android-compiler

# 添加o3mcp
claude mcp add o3mcp -s user \
  -e OPENAI_API_KEY=sk-AjLiv6wVrbxtroActCAuqeirwwBhgLx1dBy6VQaLL8hnHAGgB2ET \
  -e OPENAI_BASE_URL=https://api.oaipro.com/v1 \
  -e O3_MODEL=o3-2025-04-16 \
  -e NODE_ENV=development \
  -e LOG_LEVEL=debug \
  -- node "D:/开发项目/mcp/o3mcp/dist/index.js"

# 添加android-compiler
claude mcp add android-compiler -s user \
  -e NODE_ENV=development \
  -- wsl node /home/hua/android-compiler-mcp/index.js

# 验证配置
claude mcp list
```

## 配置完成后

1. 退出当前Claude会话：输入 `exit`
2. 重新启动Claude：运行 `claude`
3. 验证MCP加载：在聊天中输入 `/mcp`

## 常见问题

### 如果提示找不到Git
- 请从 https://git-scm.com/downloads/win 下载并安装Git for Windows

### 如果WSL路径错误
- 确认android-compiler的实际路径，可能是 `/home/hua/` 或 `/home/ua/`
- 根据实际情况修改脚本中的路径

### 如果o3mcp路径错误
- 确认o3mcp项目的实际位置
- 修改脚本中的路径 `D:/开发项目/mcp/o3mcp/dist/index.js`