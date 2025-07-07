#!/bin/bash

# 最终修复MCP服务器脚本 - 使用正确的SDK API

echo "=== 最终修复MCP服务器 ==="

# 1. 创建新的index.js使用正确的API
echo "1. 创建修复后的index.js..."
cat > /home/ua/android-compiler-mcp/index.js << 'EOF'
#!/usr/bin/env node

import { Server } from '@modelcontextprotocol/sdk/server/index.js';
import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';
import { 
  CallToolRequestSchema, 
  ListToolsRequestSchema 
} from '@modelcontextprotocol/sdk/types.js';
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

// 注册工具列表处理器
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: 'compile_kotlin',
        description: 'Compile Android Kotlin project',
        inputSchema: {
          type: 'object',
          properties: {
            projectPath: {
              type: 'string',
              description: 'Project root directory path',
            },
            module: {
              type: 'string',
              description: 'Specific module to compile',
            },
            task: {
              type: 'string',
              description: 'Gradle task to run',
              default: 'compileDebugKotlin',
            },
          },
          required: ['projectPath'],
        },
      },
      {
        name: 'check_gradle',
        description: 'Check Gradle and Android environment',
        inputSchema: {
          type: 'object',
          properties: {
            projectPath: {
              type: 'string',
              description: 'Project root directory path',
            },
          },
          required: ['projectPath'],
        },
      },
    ],
  };
});

// 处理工具调用
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;
  
  try {
    if (name === 'compile_kotlin') {
      const projectPath = path.resolve(args.projectPath);
      const module = args.module;
      const task = args.task || 'compileDebugKotlin';
      
      // 检查项目路径是否存在
      try {
        await fs.access(projectPath);
      } catch (error) {
        return {
          isError: true,
          content: [{
            type: 'text',
            text: `Error: Project path does not exist: ${projectPath}`,
          }],
        };
      }
      
      // 设置环境变量
      const env = {
        ...process.env,
        ANDROID_HOME: '/mnt/c/Users/Hua/AppData/Local/Android/Sdk',
        ANDROID_SDK_ROOT: '/mnt/c/Users/Hua/AppData/Local/Android/Sdk',
      };
      
      // 构建Gradle命令
      let gradleCmd = './gradlew';
      if (module) {
        gradleCmd += ` :${module}:${task}`;
      } else {
        gradleCmd += ` ${task}`;
      }
      
      // 添加跳过Android特定任务的参数
      gradleCmd += ' -x lint -x processDebugManifest -x processDebugResources -x mergeDebugResources';
      gradleCmd += ' --no-daemon --console=plain';
      
      console.error(`Executing: ${gradleCmd} in ${projectPath}`);
      
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
          isError: true,
          content: [{
            type: 'text',
            text: `Compilation failed:\n${error.message}\n\nStderr:\n${error.stderr}`,
          }],
        };
      }
    } else if (name === 'check_gradle') {
      const projectPath = path.resolve(args.projectPath);
      
      // 检查项目路径是否存在
      try {
        await fs.access(projectPath);
      } catch (error) {
        return {
          isError: true,
          content: [{
            type: 'text',
            text: `Error: Project path does not exist: ${projectPath}`,
          }],
        };
      }
      
      // 检查Gradle wrapper
      const gradlewPath = path.join(projectPath, 'gradlew');
      const gradlewExists = await fs.access(gradlewPath).then(() => true).catch(() => false);
      
      if (!gradlewExists) {
        return {
          isError: true,
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
          isError: true,
          content: [{
            type: 'text',
            text: `Failed to check Gradle:\n${error.message}`,
          }],
        };
      }
    } else {
      return {
        isError: true,
        content: [{
          type: 'text',
          text: `Unknown tool: ${name}`,
        }],
      };
    }
  } catch (error) {
    return {
      isError: true,
      content: [{
        type: 'text',
        text: `Error: ${error.message}`,
      }],
    };
  }
});

// 启动服务器
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error('MCP server started successfully');
}

main().catch((error) => {
  console.error('Failed to start server:', error);
  process.exit(1);
});
EOF

echo "2. 完成！请重启Claude Code以使MCP服务器生效。"
echo ""
echo "使用方法："
echo "  1. 退出当前Claude Code会话"
echo "  2. 重新运行: claude"
echo ""
echo "MCP服务器将在Claude Code启动时自动加载。"