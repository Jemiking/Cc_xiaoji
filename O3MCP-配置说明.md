# O3MCP 配置说明

## 已完成的准备工作

1. ✅ o3mcp项目已构建完成（dist目录已生成）
2. ✅ .env文件已配置（包含OpenAI API密钥）
3. ✅ 创建了MCP配置文件示例

## 手动配置步骤

### 方法一：使用Git Bash（推荐）

1. 安装Git for Windows（如果还没有安装）：https://git-scm.com/downloads/win

2. 打开Git Bash，运行以下命令：
```bash
claude mcp add o3mcp -s user -- node "D:/开发项目/mcp/o3mcp/dist/index.js"
```

3. 重启Claude Code：
```bash
# 退出当前Claude会话
exit

# 重新启动Claude
claude
```

### 方法二：手动编辑配置文件

1. 找到Claude Code的配置文件位置：
   - Windows: `%APPDATA%\claude\claude_code_config.json`
   - 或者: `%USERPROFILE%\.claude\config.json`
   - 或者: `%USERPROFILE%\.config\claude\config.json`

2. 将以下内容添加到配置文件的`mcpServers`部分：
```json
{
  "mcpServers": {
    "o3mcp": {
      "command": "node",
      "args": ["D:\\开发项目\\mcp\\o3mcp\\dist\\index.js"],
      "env": {
        "OPENAI_API_KEY": "sk-AjLiv6wVrbxtroActCAuqeirwwBhgLx1dBy6VQaLL8hnHAGgB2ET",
        "OPENAI_BASE_URL": "https://api.oaipro.com/v1",
        "O3_MODEL": "o3-2025-04-16",
        "NODE_ENV": "development",
        "LOG_LEVEL": "debug"
      }
    }
  }
}
```

3. 如果配置文件中已有其他MCP服务器（如android-compiler），则添加o3mcp到现有的mcpServers对象中。

4. 保存文件并重启Claude Code。

## 验证配置

重启Claude Code后，可以使用以下命令测试o3mcp是否工作：

```
使用understand_with_o3工具分析：我需要优化数据库查询性能
```

## 配置文件示例位置

已在项目根目录创建了配置示例文件：`D:\kotlin\Cc_xiaoji\o3mcp-config.json`

## 故障排除

1. 如果找不到配置文件：
   - 尝试运行 `claude mcp list` 查看当前配置
   - 检查 `~/.claude/` 目录

2. 如果MCP无法启动：
   - 确保Node.js已安装：`node --version`
   - 确保o3mcp项目路径正确
   - 检查.env文件中的API密钥是否有效

3. 查看MCP日志：
   - Windows: `%APPDATA%\claude\logs\`
   - 或查看: `~/.cache/claude-cli-nodejs/*/mcp-logs-o3mcp/`