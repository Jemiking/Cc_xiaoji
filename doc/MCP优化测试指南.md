# MCP Android Compiler优化测试指南

## 测试前准备

1. **确认版本已切换**
   - 检查 `android-compiler-mcp-windows/index.js` 文件
   - 应该看到 `version: '2.0.0'`

2. **重启Claude Code**
   - 关闭所有Claude Code实例
   - 重新打开项目

## 测试步骤

### 步骤1：验证MCP连接

```
使用 mcp__android-compiler__check_gradle 工具
参数：projectPath="."
```

期望输出：
- Gradle版本信息
- 项目结构
- ANDROID_HOME路径

### 步骤2：测试预构建功能

```
使用 mcp__android-compiler__prepare_android_build 工具
参数：
  projectPath="."
  module="app"
```

期望输出：
```
Android build preparation results:
✓ clean completed
✓ preBuild completed
✓ generateDebugSources completed
✓ processDebugManifest completed
✓ generateDebugRFile completed
```

### 步骤3：测试优化编译

```
使用 mcp__android-compiler__compile_kotlin 工具
参数：
  projectPath="."
  task="compileDebugKotlin"
  module="app"
```

期望结果：
- 编译成功
- 自动执行预构建步骤
- 输出包含 "BUILD SUCCESSFUL"

### 步骤4：测试单元测试编译

```
使用 mcp__android-compiler__compile_kotlin 工具
参数：
  projectPath="."
  task="compileDebugUnitTestKotlin"
  module="app"
```

期望结果：
- 测试代码编译成功
- 保留了必要的资源处理步骤

### 步骤5：测试回退机制

如果优化版本出现问题：

```
使用 mcp__android-compiler__compile_kotlin 工具
参数：
  projectPath="."
  task="compileDebugKotlin"
  module="app"
  skipOptimization=true
```

这将使用原始的编译行为。

## 性能测试

### 测试1：编译时间对比

记录以下任务的执行时间：

1. 原版本编译时间（使用skipOptimization=true）
2. 优化版本编译时间（默认）
3. 优化版本无预构建（preBuild=false）

### 测试2：成功率对比

测试以下场景的成功率：

| 场景 | 原版本 | 优化版本 |
|------|--------|----------|
| 主代码编译 | ? | ? |
| 测试代码编译 | ? | ? |
| 多模块编译 | ? | ? |
| 清理后编译 | ? | ? |

## 问题诊断

### 如果编译失败

1. **查看错误信息**
   - 是否包含 "AndroidManifest.xml" 相关错误？
   - 是否有具体的任务失败信息？

2. **尝试修复步骤**
   ```
   # 步骤1：准备构建环境
   mcp__android-compiler__prepare_android_build
   
   # 步骤2：重试编译
   mcp__android-compiler__compile_kotlin
   
   # 步骤3：如果仍失败，使用兼容模式
   mcp__android-compiler__compile_kotlin(skipOptimization=true)
   ```

3. **收集诊断信息**
   - 错误消息
   - 使用的参数
   - 项目状态（是否刚clean过）

## 测试报告模板

```markdown
## MCP优化版本测试报告

**测试时间**：2025-07-XX
**测试人员**：XXX
**MCP版本**：2.0.0

### 功能测试结果

- [ ] check_gradle - 正常
- [ ] prepare_android_build - 正常
- [ ] compile_kotlin（主代码）- 正常
- [ ] compile_kotlin（测试代码）- 正常
- [ ] skipOptimization参数 - 正常

### 性能测试结果

| 任务 | 原版本耗时 | 优化版本耗时 | 提升 |
|------|------------|--------------|------|
| 主代码编译 | XXs | XXs | XX% |
| 测试编译 | XXs | XXs | XX% |

### 问题记录

1. 问题描述：
   解决方案：

### 总体评价

- 稳定性：⭐⭐⭐⭐⭐
- 性能提升：⭐⭐⭐⭐⭐
- 兼容性：⭐⭐⭐⭐⭐
```

## 回滚指南

如果需要回滚到原版本：

```batch
# 运行切换脚本
scripts\switch-mcp-version.bat

# 选择选项1：切换到原始版本
```

或手动操作：
```bash
cd android-compiler-mcp-windows
cp index-original.js index.js
```

记得重启Claude Code！

---
更新时间：2025-07-25