@echo off
REM Fix MCP Servers Configuration

echo ========================================
echo 修复MCP服务器配置
echo ========================================
echo.

REM 备份原始文件
echo [1] 备份原始MCP配置...
copy /Y "D:\kotlin\Cc_xiaoji\.claude\mcp.json" "D:\kotlin\Cc_xiaoji\.claude\mcp.json.bak" >nul 2>&1
echo 完成

echo.
echo [2] 创建android-compiler包装脚本...
REM 创建一个包装脚本来过滤掉stderr输出
echo @echo off > "D:\kotlin\Cc_xiaoji\android-compiler-mcp-windows\wrapper.bat"
echo node "%~dp0index.js" %%* 2^>nul >> "D:\kotlin\Cc_xiaoji\android-compiler-mcp-windows\wrapper.bat"

echo.
echo [3] 创建o3mcp包装脚本...
REM 创建o3mcp的包装脚本，设置环境变量
echo @echo off > "D:\开发项目\mcp\o3mcp\wrapper.bat"
echo set OPENAI_API_KEY=sk-AjLiv6wVrbxtroActCAuqeirwwBhgLx1dBy6VQaLL8hnHAGgB2ET >> "D:\开发项目\mcp\o3mcp\wrapper.bat"
echo set OPENAI_BASE_URL=https://api.oaipro.com/v1 >> "D:\开发项目\mcp\o3mcp\wrapper.bat"
echo set O3_MODEL=o3-2025-04-16 >> "D:\开发项目\mcp\o3mcp\wrapper.bat"
echo set NODE_ENV=development >> "D:\开发项目\mcp\o3mcp\wrapper.bat"
echo set LOG_LEVEL=debug >> "D:\开发项目\mcp\o3mcp\wrapper.bat"
echo node "%~dp0dist\index.js" %%* >> "D:\开发项目\mcp\o3mcp\wrapper.bat"

echo.
echo [4] 更新MCP配置文件...
REM 这里手动创建新的配置

echo 修复完成！
echo.
echo 注意：需要手动更新.claude\mcp.json文件中的命令路径
echo 请将：
echo   android-compiler的command改为："cmd"
echo   android-compiler的args改为：["/c", "D:\\kotlin\\Cc_xiaoji\\android-compiler-mcp-windows\\wrapper.bat"]
echo   o3mcp的command改为："cmd"
echo   o3mcp的args改为：["/c", "D:\\开发项目\\mcp\\o3mcp\\wrapper.bat"]
echo.
pause