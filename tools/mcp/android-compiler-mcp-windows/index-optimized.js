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
    version: '2.0.0', // 优化版本
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
        description: 'Compile Android Kotlin project with optimized build process',
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
            skipOptimization: {
              type: 'boolean',
              description: 'Skip build optimization (use all exclusions)',
              default: false,
            },
            preBuild: {
              type: 'boolean',
              description: 'Run necessary pre-build tasks',
              default: true,
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
      {
        name: 'prepare_android_build',
        description: 'Prepare Android build by generating necessary files',
        inputSchema: {
          type: 'object',
          properties: {
            projectPath: {
              type: 'string',
              description: 'Project root directory path',
            },
            module: {
              type: 'string',
              description: 'Specific module to prepare',
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
      const skipOptimization = args.skipOptimization || false;
      const preBuild = args.preBuild !== false; // 默认true
      
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
      
      // 执行预构建步骤（如果需要）
      if (preBuild && !skipOptimization) {
        console.error(`Running pre-build tasks...`);
        
        // 生成必要的源文件
        let preBuildCmd = './gradlew';
        if (module) {
          preBuildCmd += ` :${module}:generateDebugSources`;
        } else {
          preBuildCmd += ' generateDebugSources';
        }
        preBuildCmd += ' --no-daemon --console=plain';
        
        try {
          console.error(`Executing pre-build: ${preBuildCmd}`);
          const { stdout, stderr } = await execAsync(preBuildCmd, {
            cwd: projectPath,
            env: env,
            maxBuffer: 10 * 1024 * 1024,
          });
          console.error('Pre-build completed successfully');
        } catch (error) {
          console.error(`Pre-build warning: ${error.message}`);
          // 继续执行，因为某些任务可能已经完成
        }
      }
      
      // 构建Gradle命令
      let gradleCmd = './gradlew';
      if (module) {
        gradleCmd += ` :${module}:${task}`;
      } else {
        gradleCmd += ` ${task}`;
      }
      
      // 优化的排除参数策略
      if (skipOptimization) {
        // 旧的行为：排除所有Android特定任务
        gradleCmd += ' -x lint -x processDebugManifest -x processDebugResources -x mergeDebugResources';
        gradleCmd += ' -x generateDebugBuildConfig -x createDebugCompatibleScreenManifests';
        gradleCmd += ' -x extractDeepLinksDebug -x processDebugMainManifest';
        gradleCmd += ' -x mergeDebugAssets -x compressDebugAssets';
        gradleCmd += ' -x mergeDebugJniLibFolders -x mergeDebugNativeLibs';
        gradleCmd += ' -x stripDebugDebugSymbols -x validateSigningDebug';
        gradleCmd += ' -x writeDebugAppMetadata -x writeDebugSigningConfigVersions';
      } else {
        // 优化的排除：只排除真正不必要的任务
        gradleCmd += ' -x lint'; // Lint检查可以跳过
        gradleCmd += ' -x stripDebugDebugSymbols'; // 符号剥离可以跳过
        gradleCmd += ' -x validateSigningDebug'; // 签名验证可以跳过
        
        // 对于测试编译，我们需要保留manifest和资源处理
        if (task.includes('Test')) {
          // 测试任务需要更多的构建步骤
          console.error('Detected test compilation, keeping resource processing tasks');
        } else {
          // 非测试任务可以跳过一些资源处理
          gradleCmd += ' -x compressDebugAssets';
          gradleCmd += ' -x mergeDebugJniLibFolders -x mergeDebugNativeLibs';
        }
      }
      
      gradleCmd += ' --no-daemon --console=plain';
      
      console.error(`Executing: ${gradleCmd} in ${projectPath}`);
      
      try {
        const { stdout, stderr } = await execAsync(gradleCmd, {
          cwd: projectPath,
          env: env,
          maxBuffer: 10 * 1024 * 1024, // 10MB
        });
        
        // 改进的输出过滤
        const output = stdout + '\n' + stderr;
        const lines = output.split('\n');
        const importantLines = lines.filter(line => {
          return line.includes('BUILD') || 
                 line.includes('FAILED') || 
                 line.includes('error:') ||
                 line.includes('e:') ||
                 line.includes('> Task') ||
                 line.includes('Execution failed') ||
                 line.includes('SUCCESSFUL') ||
                 line.includes('UP-TO-DATE') ||
                 line.includes('compil');
        });
        
        const result = importantLines.join('\n') || 'Build completed';
        
        return {
          content: [{
            type: 'text',
            text: result,
          }],
        };
      } catch (error) {
        // 改进的错误处理，提供更多诊断信息
        let errorMessage = `Compilation failed:\n${error.message}`;
        
        // 检查是否是manifest相关错误
        if (error.stderr && error.stderr.includes('AndroidManifest.xml')) {
          errorMessage += '\n\n建议：运行 prepare_android_build 工具来生成必要的文件';
        }
        
        return {
          isError: true,
          content: [{
            type: 'text',
            text: errorMessage,
          }],
        };
      }
    } else if (name === 'prepare_android_build') {
      const projectPath = path.resolve(args.projectPath);
      const module = args.module;
      
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
      
      // 构建准备命令序列
      const tasks = [
        'clean', // 清理旧的构建文件
        'preBuild', // 预构建任务
        'generateDebugSources', // 生成源文件
        'processDebugManifest', // 处理manifest
        'generateDebugRFile', // 生成R文件
      ];
      
      let results = [];
      
      for (const task of tasks) {
        let gradleCmd = './gradlew';
        if (module) {
          gradleCmd += ` :${module}:${task}`;
        } else {
          gradleCmd += ` ${task}`;
        }
        gradleCmd += ' --no-daemon --console=plain';
        
        console.error(`Executing: ${gradleCmd}`);
        
        try {
          const { stdout, stderr } = await execAsync(gradleCmd, {
            cwd: projectPath,
            env: env,
            maxBuffer: 10 * 1024 * 1024,
          });
          results.push(`✓ ${task} completed`);
        } catch (error) {
          results.push(`✗ ${task} failed: ${error.message.split('\n')[0]}`);
          // 继续执行其他任务
        }
      }
      
      return {
        content: [{
          type: 'text',
          text: `Android build preparation results:\n${results.join('\n')}`,
        }],
      };
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
        // 检查Gradle版本
        const { stdout: gradleVersion } = await execAsync('./gradlew --version', {
          cwd: projectPath,
          env: env,
        });
        
        // 检查项目结构
        const { stdout: projectInfo } = await execAsync('./gradlew projects --no-daemon', {
          cwd: projectPath,
          env: env,
        });
        
        return {
          content: [{
            type: 'text',
            text: `Gradle environment check:\n${gradleVersion}\n\nProject structure:\n${projectInfo}\n\nANDROID_HOME: ${env.ANDROID_HOME}`,
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
  console.error('MCP Android Compiler Server v2.0 (Optimized) started successfully');
}

main().catch((error) => {
  console.error('Failed to start server:', error);
  process.exit(1);
});