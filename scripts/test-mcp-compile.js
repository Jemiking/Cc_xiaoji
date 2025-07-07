#!/usr/bin/env node

// 测试MCP Kotlin编译器功能的脚本
import { spawn } from 'child_process';
import readline from 'readline';

const MCP_SERVER_PATH = '/mnt/d/kotlin/mcp-kotlin-compiler/dist/server.js';
const TEST_PROJECT_PATH = '/mnt/d/kotlin/mcp-kotlin-compiler/test-project';

console.log('=== 测试MCP Kotlin编译器 ===\n');

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
    console.log('服务器响应:', JSON.stringify(response, null, 2));
    
    // 根据响应ID执行下一步
    if (response.id === 1 && response.result) {
      // 收到工具列表后，设置项目路径
      sendSetProjectPath();
    } else if (response.id === 2 && response.result) {
      // 设置路径成功后，编译项目
      sendCompileProject();
    } else if (response.id === 3) {
      // 编译完成，显示结果后退出
      console.log('\n编译结果:', response.result?.content?.[0]?.text || '未知结果');
      setTimeout(() => {
        server.kill();
        process.exit(0);
      }, 1000);
    }
  } catch (e) {
    console.log('服务器输出:', line);
  }
});

// 监听错误
server.stderr.on('data', (data) => {
  console.error('服务器错误:', data.toString());
});

server.on('error', (error) => {
  console.error('启动失败:', error.message);
  process.exit(1);
});

// 发送JSON-RPC请求的辅助函数
function sendRequest(method, params, id) {
  const request = JSON.stringify({
    jsonrpc: '2.0',
    method,
    params,
    id
  }) + '\n';
  
  console.log(`\n发送请求 ${id}: ${method}`);
  server.stdin.write(request);
}

// 1. 列出可用工具
function sendListTools() {
  sendRequest('tools/list', {}, 1);
}

// 2. 设置项目路径
function sendSetProjectPath() {
  sendRequest('tools/call', {
    name: 'set_project_path',
    arguments: {
      path: TEST_PROJECT_PATH
    }
  }, 2);
}

// 3. 编译项目
function sendCompileProject() {
  sendRequest('tools/call', {
    name: 'compile_project',
    arguments: {}
  }, 3);
}

// 开始测试
setTimeout(() => {
  sendListTools();
}, 500);

// 超时退出
setTimeout(() => {
  console.log('\n测试超时，强制退出');
  server.kill();
  process.exit(1);
}, 30000);