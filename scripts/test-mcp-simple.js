// Simple MCP server test
const { spawn } = require('child_process');

console.log('Testing MCP servers...\n');

// Test android-compiler
console.log('[1] Testing android-compiler...');
const androidCompiler = spawn('node', ['D:/kotlin/Cc_xiaoji/android-compiler-mcp-windows/index.js']);

androidCompiler.stdin.write(JSON.stringify({
  jsonrpc: '2.0',
  method: 'tools/list',
  id: 1
}) + '\n');

let androidResponse = '';
androidCompiler.stdout.on('data', (data) => {
  androidResponse += data.toString();
});

androidCompiler.stderr.on('data', (data) => {
  console.error('Android compiler error:', data.toString());
});

// Test o3mcp
console.log('[2] Testing o3mcp...');
const o3mcp = spawn('node', ['D:/开发项目/mcp/o3mcp/dist/index.js']);

o3mcp.stdin.write(JSON.stringify({
  jsonrpc: '2.0',
  method: 'tools/list',
  id: 1
}) + '\n');

let o3Response = '';
o3mcp.stdout.on('data', (data) => {
  o3Response += data.toString();
});

o3mcp.stderr.on('data', (data) => {
  console.error('O3MCP error:', data.toString());
});

// Wait and show results
setTimeout(() => {
  console.log('\nResults:');
  console.log('Android compiler response:', androidResponse ? 'OK - Got response' : 'ERROR - No response');
  if (androidResponse) {
    try {
      const parsed = JSON.parse(androidResponse.split('\n').find(line => line.includes('compile_kotlin')));
      console.log('  - Found compile_kotlin tool');
    } catch (e) {
      console.log('  - Response:', androidResponse.substring(0, 100) + '...');
    }
  }
  
  console.log('\nO3MCP response:', o3Response ? 'OK - Got response' : 'ERROR - No response');
  if (o3Response) {
    try {
      const parsed = JSON.parse(o3Response.split('\n').find(line => line.includes('understand_with_o3')));
      console.log('  - Found understand_with_o3 tool');
    } catch (e) {
      console.log('  - Response:', o3Response.substring(0, 100) + '...');
    }
  }
  
  // Kill processes
  androidCompiler.kill();
  o3mcp.kill();
  
  console.log('\nTest completed. If both servers responded with OK, the configuration is correct.');
  console.log('\nNOTE: You need to restart Claude Code for the changes to take effect:');
  console.log('1. Type "exit" in Claude Code');
  console.log('2. Start Claude Code again with "claude"');
  
  process.exit(0);
}, 2000);