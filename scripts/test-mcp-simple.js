#!/usr/bin/env node

// 简单测试MCP编译单个文件的功能
import { spawn } from 'child_process';
import readline from 'readline';

const MCP_SERVER_PATH = '/mnt/d/kotlin/mcp-kotlin-compiler/dist/server.js';
const TEST_FILE_PATH = '/mnt/d/kotlin/mcp-kotlin-compiler/test-project/src/main/kotlin/com/example/Main.kt';

console.log('=== 测试MCP编译单个文件 ===\n');

// 启动MCP服务器
const server = spawn('node', [MCP_SERVER_PATH], {
  stdio: ['pipe', 'pipe', 'pipe'],
  env: { ...process.env, NODE_ENV: 'production' }
});

// 创建读取接口
const rl = readline.createInterface({
  input: server.stdout,
  crlfDelay: Infinity
});

// 监听服务器响应
rl.on('line', (line) => {
  try {
    const response = JSON.parse(line);
    console.log('响应:', JSON.stringify(response.result?.content?.[0]?.text || response.result, null, 2));
    
    if (response.id === 1) {
      // 设置项目路径后，编译文件
      sendCompileFile();
    } else if (response.id === 2) {
      // 编译完成，获取错误信息
      sendGetErrors();
    } else if (response.id === 3) {
      // 显示错误后退出
      setTimeout(() => {
        server.kill();
        process.exit(0);
      }, 1000);
    }
  } catch (e) {
    // 忽略非JSON输出
  }
});

// 发送请求
function sendRequest(method, params, id) {
  const request = JSON.stringify({
    jsonrpc: '2.0',
    method,
    params,
    id
  }) + '\n';
  
  console.log(`\n发送: ${params.name || method}`);
  server.stdin.write(request);
}

// 1. 设置项目路径
function sendSetProjectPath() {
  sendRequest('tools/call', {
    name: 'set_project_path',
    arguments: {
      path: '/mnt/d/kotlin/mcp-kotlin-compiler/test-project'
    }
  }, 1);
}

// 2. 编译文件
function sendCompileFile() {
  sendRequest('tools/call', {
    name: 'compile_file',
    arguments: {
      filePath: TEST_FILE_PATH
    }
  }, 2);
}

// 3. 获取错误
function sendGetErrors() {
  sendRequest('tools/call', {
    name: 'get_errors',
    arguments: {
      filePath: TEST_FILE_PATH
    }
  }, 3);
}

// 开始测试
setTimeout(() => {
  sendSetProjectPath();
}, 500);

// 超时退出
setTimeout(() => {
  console.log('\n测试超时');
  server.kill();
  process.exit(1);
}, 30000);