@echo off
:: MCP优化版本快速测试脚本
:: 用于验证优化后的Android Compiler MCP是否正常工作

echo ========================================
echo MCP Android Compiler 优化版本测试
echo ========================================
echo.
echo 注意：请确保已经重启Claude Code！
echo.
pause

echo.
echo [测试说明]
echo 本脚本将帮助您测试优化后的MCP功能
echo 请在Claude Code中依次执行以下命令：
echo.

echo ========================================
echo 测试1：检查Gradle环境
echo ========================================
echo 在Claude Code中执行：
echo.
echo mcp__android-compiler__check_gradle 工具
echo 参数：projectPath="."
echo.
echo 期望结果：显示Gradle版本和项目结构
echo ========================================
echo.
pause

echo.
echo ========================================
echo 测试2：准备Android构建环境
echo ========================================
echo 在Claude Code中执行：
echo.
echo mcp__android-compiler__prepare_android_build 工具
echo 参数：projectPath="." module="app"
echo.
echo 期望结果：5个任务都显示✓完成
echo ========================================
echo.
pause

echo.
echo ========================================
echo 测试3：编译主代码（优化模式）
echo ========================================
echo 在Claude Code中执行：
echo.
echo mcp__android-compiler__compile_kotlin 工具
echo 参数：projectPath="." task="compileDebugKotlin" module="app"
echo.
echo 期望结果：BUILD SUCCESSFUL
echo ========================================
echo.
pause

echo.
echo ========================================
echo 测试4：编译测试代码（关键测试）
echo ========================================
echo 在Claude Code中执行：
echo.
echo mcp__android-compiler__compile_kotlin 工具
echo 参数：projectPath="." task="compileDebugUnitTestKotlin" module="app"
echo.
echo 期望结果：测试代码编译成功
echo 注：这是优化版本的主要改进点
echo ========================================
echo.
pause

echo.
echo ========================================
echo 测试5：兼容模式测试
echo ========================================
echo 在Claude Code中执行：
echo.
echo mcp__android-compiler__compile_kotlin 工具
echo 参数：projectPath="." task="compileDebugKotlin" module="app" skipOptimization=true
echo.
echo 期望结果：使用原始编译行为
echo ========================================
echo.
pause

echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
echo 请记录测试结果：
echo.
echo [ ] 测试1 - Gradle环境检查：通过/失败
echo [ ] 测试2 - 构建环境准备：通过/失败
echo [ ] 测试3 - 主代码编译：通过/失败
echo [ ] 测试4 - 测试代码编译：通过/失败
echo [ ] 测试5 - 兼容模式：通过/失败
echo.
echo 如果所有测试都通过，说明优化版本工作正常！
echo.
echo 如需回滚，请运行：scripts\switch-mcp-version.bat
echo.
pause