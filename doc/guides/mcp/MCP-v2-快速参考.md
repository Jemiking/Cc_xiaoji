# MCP Android Compiler v2.0 快速参考

## 🚀 快速开始

**重要：切换版本后必须重启Claude Code！**

## 📋 新功能一览

### 1. 准备构建环境
```
mcp__android-compiler__prepare_android_build
参数：projectPath="." module="app"
```

### 2. 优化编译（默认）
```
mcp__android-compiler__compile_kotlin
参数：projectPath="." task="compileDebugKotlin" module="app"
```

### 3. 测试编译（已修复）
```
mcp__android-compiler__compile_kotlin
参数：projectPath="." task="compileDebugUnitTestKotlin" module="app"
```

### 4. 兼容模式
```
mcp__android-compiler__compile_kotlin
参数：projectPath="." module="app" skipOptimization=true
```

## 🛠️ 新增参数

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| skipOptimization | boolean | false | 使用原始行为 |
| preBuild | boolean | true | 执行预构建 |

## 🔧 常见场景

### 场景1：首次使用或clean后
```
1. prepare_android_build
2. compile_kotlin
```

### 场景2：测试编译失败
```
1. prepare_android_build module="app"
2. compile_kotlin task="compileDebugUnitTestKotlin"
```

### 场景3：遇到问题
```
compile_kotlin skipOptimization=true
```

## 📦 版本切换

```batch
scripts\switch-mcp-version.bat
```

## ✅ 主要改进

- **测试编译**：从总是失败 → 正常工作
- **错误提示**：模糊信息 → 清晰建议
- **构建准备**：手动处理 → 自动执行
- **兼容性**：完全向后兼容

---
v2.0 | 2025-07-25