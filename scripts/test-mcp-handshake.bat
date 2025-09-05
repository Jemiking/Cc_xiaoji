@echo off
REM MCP握手测试脚本
REM 测试android-compiler MCP服务器是否能正确响应握手协议

echo ========================================
echo MCP握手协议测试
echo ========================================
echo.

REM 切换到项目目录
cd /d D:\kotlin\Cc_xiaoji

echo [1] 测试直接运行MCP服务器...
echo 期望输出: MCP/1 HELLO
echo.

REM 创建临时测试文件来发送握手请求
echo {"jsonrpc":"2.0","method":"hello","params":{},"id":1} > test-handshake.tmp

REM 运行脚本并测试握手
echo [2] 通过wrapper脚本启动MCP并发送握手...
type test-handshake.tmp | scripts\android-compiler-wrapper-fixed.bat

REM 清理临时文件
del test-handshake.tmp

echo.
echo [3] 测试完成！
echo 如果看到MCP/1 HELLO响应，说明握手成功
pause