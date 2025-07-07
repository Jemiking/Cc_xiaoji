// 测试MCP Kotlin编译器服务器的脚本
import { spawn } from 'child_process';

console.log('=== 测试MCP Kotlin编译器服务器 ===\n');

// 启动MCP服务器
const serverPath = '/mnt/d/kotlin/mcp-kotlin-compiler/dist/server.js';
const server = spawn('node', [serverPath], {
  stdio: ['pipe', 'pipe', 'pipe']
});

// 监听服务器输出
server.stdout.on('data', (data) => {
  console.log(`服务器输出: ${data}`);
});

server.stderr.on('data', (data) => {
  console.error(`服务器错误: ${data}`);
});

server.on('error', (error) => {
  console.error(`启动失败: ${error.message}`);
});

// 发送测试请求
setTimeout(() => {
  // 列出可用工具
  const listToolsRequest = JSON.stringify({
    jsonrpc: '2.0',
    method: 'tools/list',
    id: 1
  }) + '\n';
  
  console.log('发送请求: 列出可用工具');
  server.stdin.write(listToolsRequest);
}, 1000);

// 5秒后关闭
setTimeout(() => {
  console.log('\n测试完成，关闭服务器');
  server.kill();
  process.exit(0);
}, 5000);