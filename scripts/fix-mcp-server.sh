#!/bin/bash

# 修复MCP服务器脚本

echo "=== 修复MCP服务器 ==="

# 1. 备份原始文件
echo "1. 备份原始文件..."
cp /home/ua/android-compiler-mcp/index.js /home/ua/android-compiler-mcp/index.js.backup
cp /home/ua/android-compiler-mcp/package.json /home/ua/android-compiler-mcp/package.json.backup

# 2. 更新package.json添加缺失的依赖
echo "2. 更新package.json..."
cat > /home/ua/android-compiler-mcp/package.json << 'EOF'
{
  "name": "android-compiler-mcp",
  "version": "1.0.0",
  "description": "MCP server for Android compilation",
  "main": "index.js",
  "type": "module",
  "scripts": {
    "start": "node index.js"
  },
  "dependencies": {
    "@modelcontextprotocol/sdk": "^0.5.0",
    "zod": "^3.22.4"
  }
}
EOF

# 3. 修复index.js中的API调用问题
echo "3. 修复index.js..."
cat > /home/ua/android-compiler-mcp/index.js << 'EOF'
#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { z } from 'zod';
import { exec } from 'child_process';
import { promisify } from 'util';
import path from 'path';
import fs from 'fs/promises';

const execAsync = promisify(exec);

// 创建MCP服务器
const server = new Server(
  {
    name: 'android-compiler',
    version: '1.0.0',
  },
  {
    capabilities: {
      tools: {},
    },
  }
);

// 编译Kotlin项目的工具
const CompileKotlinTool = {
  name: 'compile_kotlin',
  description: 'Compile Android Kotlin project',
  inputSchema: z.object({
    projectPath: z.string().describe('Project root directory path'),
    module: z.string().optional().describe('Specific module to compile'),
    task: z.string().default('compileDebugKotlin').describe('Gradle task to run'),
  }),
};

// 检查Gradle环境的工具
const CheckGradleTool = {
  name: 'check_gradle',
  description: 'Check Gradle and Android environment',
  inputSchema: z.object({
    projectPath: z.string().describe('Project root directory path'),
  }),
};

// 注册工具
server.addTool({
  name: CompileKotlinTool.name,
  description: CompileKotlinTool.description,
  inputSchema: CompileKotlinTool.inputSchema,
});

server.addTool({
  name: CheckGradleTool.name,
  description: CheckGradleTool.description,
  inputSchema: CheckGradleTool.inputSchema,
});

// 处理工具调用
server.setRequestHandler('tools/call', async (request) => {
  const { name, arguments: args } = request.params;
  
  try {
    if (name === 'compile_kotlin') {
      const parsedArgs = CompileKotlinTool.inputSchema.parse(args);
      const projectPath = path.resolve(parsedArgs.projectPath);
      
      // 设置环境变量
      const env = {
        ...process.env,
        ANDROID_HOME: '/mnt/c/Users/Hua/AppData/Local/Android/Sdk',
        ANDROID_SDK_ROOT: '/mnt/c/Users/Hua/AppData/Local/Android/Sdk',
      };
      
      // 构建Gradle命令
      let gradleCmd = './gradlew';
      if (parsedArgs.module) {
        gradleCmd += ` :${parsedArgs.module}:${parsedArgs.task}`;
      } else {
        gradleCmd += ` ${parsedArgs.task}`;
      }
      
      // 添加跳过Android特定任务的参数
      gradleCmd += ' -x lint -x processDebugManifest -x processDebugResources -x mergeDebugResources';
      gradleCmd += ' --no-daemon --console=plain';
      
      try {
        const { stdout, stderr } = await execAsync(gradleCmd, {
          cwd: projectPath,
          env: env,
          maxBuffer: 10 * 1024 * 1024, // 10MB
        });
        
        // 过滤输出，只显示重要信息
        const output = stdout + '\n' + stderr;
        const lines = output.split('\n');
        const filteredLines = lines.filter(line => {
          return line.includes('BUILD') || 
                 line.includes('FAILED') || 
                 line.includes('error:') ||
                 line.includes('e:') ||
                 line.includes('> Task') ||
                 line.includes('Execution failed');
        });
        
        const result = filteredLines.join('\n') || 'Build completed successfully';
        
        return {
          content: [{
            type: 'text',
            text: result,
          }],
        };
      } catch (error) {
        return {
          content: [{
            type: 'text',
            text: `Compilation failed:\n${error.message}\n\nStderr:\n${error.stderr}`,
          }],
        };
      }
    } else if (name === 'check_gradle') {
      const parsedArgs = CheckGradleTool.inputSchema.parse(args);
      const projectPath = path.resolve(parsedArgs.projectPath);
      
      // 检查Gradle wrapper
      const gradlewPath = path.join(projectPath, 'gradlew');
      const gradlewExists = await fs.access(gradlewPath).then(() => true).catch(() => false);
      
      if (!gradlewExists) {
        return {
          content: [{
            type: 'text',
            text: 'Error: gradlew not found in project directory',
          }],
        };
      }
      
      // 设置环境变量
      const env = {
        ...process.env,
        ANDROID_HOME: '/mnt/c/Users/Hua/AppData/Local/Android/Sdk',
        ANDROID_SDK_ROOT: '/mnt/c/Users/Hua/AppData/Local/Android/Sdk',
      };
      
      try {
        const { stdout } = await execAsync('./gradlew --version', {
          cwd: projectPath,
          env: env,
        });
        
        return {
          content: [{
            type: 'text',
            text: `Gradle environment check:\n${stdout}\n\nANDROID_HOME: ${env.ANDROID_HOME}`,
          }],
        };
      } catch (error) {
        return {
          content: [{
            type: 'text',
            text: `Failed to check Gradle:\n${error.message}`,
          }],
        };
      }
    } else {
      return {
        content: [{
          type: 'text',
          text: `Unknown tool: ${name}`,
        }],
      };
    }
  } catch (error) {
    return {
      content: [{
        type: 'text',
        text: `Error: ${error.message}`,
      }],
    };
  }
});

// 启动服务器
const transport = new StdioServerTransport();
server.connect(transport);
EOF

# 4. 重新安装依赖
echo "4. 重新安装依赖..."
cd /home/ua/android-compiler-mcp && npm install

# 5. 重启Claude Code使MCP服务器生效
echo "5. 完成！请重启Claude Code以使MCP服务器生效。"
echo ""
echo "提示：使用以下命令重启Claude Code："
echo "  1. 退出当前Claude Code会话"
echo "  2. 重新运行: claude"
echo ""
echo "MCP服务器将在Claude Code启动时自动启动。"