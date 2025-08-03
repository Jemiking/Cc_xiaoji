# MCP Android Compiler优化方案说明

## 优化背景

MCP (Model Context Protocol) 的Android编译器在处理Android项目时存在以下问题：
1. 过度排除构建步骤，导致必要的中间文件（如AndroidManifest.xml）未生成
2. 测试编译失败率高
3. 缺乏预构建支持
4. 错误诊断信息不足

## 优化内容

### 1. 智能排除策略

**原版本（v1.0.0）**：无差别排除所有Android资源处理任务
```javascript
gradleCmd += ' -x lint -x processDebugManifest -x processDebugResources ...'
// 共排除14个任务
```

**优化版本（v2.0.0）**：智能判断，只排除真正不必要的任务
```javascript
// 基础排除（所有任务都可以跳过的）
gradleCmd += ' -x lint';              // Lint检查
gradleCmd += ' -x stripDebugDebugSymbols';   // 符号剥离
gradleCmd += ' -x validateSigningDebug';     // 签名验证

// 条件排除（根据任务类型）
if (task.includes('Test')) {
    // 测试任务保留所有资源处理
} else {
    // 非测试任务可以跳过部分资源处理
    gradleCmd += ' -x compressDebugAssets';
    gradleCmd += ' -x mergeDebugJniLibFolders -x mergeDebugNativeLibs';
}
```

### 2. 预构建支持

新增自动预构建功能：
```javascript
if (preBuild && !skipOptimization) {
    // 自动执行 generateDebugSources
    // 生成必要的 AndroidManifest.xml、R文件等
}
```

### 3. 新增工具：prepare_android_build

专门用于准备Android构建环境的工具，执行以下任务序列：
1. `clean` - 清理旧构建文件
2. `preBuild` - 预构建任务
3. `generateDebugSources` - 生成源文件
4. `processDebugManifest` - 处理manifest
5. `generateDebugRFile` - 生成R文件

### 4. 改进的错误诊断

优化版本提供更详细的错误信息和建议：
```javascript
if (error.stderr && error.stderr.includes('AndroidManifest.xml')) {
    errorMessage += '\n\n建议：运行 prepare_android_build 工具来生成必要的文件';
}
```

### 5. 新增参数

**compile_kotlin** 工具新增参数：
- `skipOptimization` (boolean) - 跳过优化，使用原始行为
- `preBuild` (boolean) - 是否执行预构建步骤（默认true）

## 使用指南

### 1. 切换版本

使用提供的批处理脚本切换版本：
```batch
scripts\switch-mcp-version.bat
```

### 2. 测试编译

使用优化版本编译测试代码：
```
# 使用默认优化行为
mcp__android-compiler__compile_kotlin(
    projectPath=".", 
    task="compileDebugUnitTestKotlin",
    module="app"
)

# 如果遇到问题，可以禁用优化
mcp__android-compiler__compile_kotlin(
    projectPath=".", 
    task="compileDebugUnitTestKotlin",
    module="app",
    skipOptimization=true
)
```

### 3. 准备构建环境

如果遇到manifest相关错误：
```
mcp__android-compiler__prepare_android_build(
    projectPath=".",
    module="app"
)
```

## 性能对比

| 场景 | 原版本 | 优化版本 |
|------|---------|----------|
| 纯Kotlin编译 | ✅ 快速 | ✅ 快速 |
| 测试编译 | ❌ 常失败 | ✅ 正常 |
| 资源处理 | ❌ 全部跳过 | ✅ 智能保留 |
| 错误诊断 | ❌ 信息少 | ✅ 详细建议 |

## 注意事项

1. **需要重启Claude Code**：切换版本后必须重启Claude Code才能生效
2. **兼容性**：优化版本向后兼容，可通过`skipOptimization`参数使用原始行为
3. **首次使用**：建议先运行`prepare_android_build`确保环境准备就绪

## 故障排除

### 问题1：切换版本后无效果
**解决**：确保重启了Claude Code

### 问题2：编译仍然失败
**解决**：
1. 运行 `prepare_android_build`
2. 使用 `skipOptimization=true` 参数
3. 检查项目配置是否正确

### 问题3：性能下降
**解决**：对于简单的Kotlin编译，可以使用`preBuild=false`跳过预构建步骤

## 后续计划

1. 根据项目类型自动选择最优策略
2. 支持更多Gradle任务类型
3. 集成增量编译优化
4. 添加缓存机制

---
版本：1.0
更新时间：2025-07-25
作者：Claude