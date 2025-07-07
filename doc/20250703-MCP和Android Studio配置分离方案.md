# MCP和Android Studio配置分离方案

> **创建日期**: 2025-07-03
> **问题背景**: MCP (WSL2) 和 Android Studio (Windows) 需要不同的配置

## 问题说明

- **MCP环境**: 需要WSL路径格式 (`/mnt/c/...`)
- **Android Studio**: 需要Windows路径格式 (`C:\...`)
- **冲突文件**: `local.properties` 中的 `sdk.dir`

## 推荐的配置方案

### 方案1：保持Windows配置（推荐）

**原则**: 保持项目配置为Android Studio可用，MCP仅作代码编写工具

```properties
# local.properties - 保持Windows格式
sdk.dir=C:\\Users\\Hua\\AppData\\Local\\Android\\Sdk
```

**MCP使用方式**:
1. 编写代码
2. 不执行编译
3. 让Android Studio自动编译

### 方案2：使用环境变量

在MCP脚本中临时设置环境变量：

```bash
# 在MCP编译脚本中添加
export ANDROID_HOME=/mnt/c/Users/Hua/AppData/Local/Android/Sdk
export ANDROID_SDK_ROOT=$ANDROID_HOME
```

### 方案3：创建独立的MCP配置文件

```bash
# local.properties.mcp - MCP专用配置
sdk.dir=/mnt/c/Users/Hua/AppData/Local/Android/Sdk

# 使用时临时替换
cp local.properties.mcp local.properties
./gradlew compileDebugKotlin
cp local.properties.windows local.properties
```

## 当前推荐的工作流程

### 1. 配置保持

- ✅ `local.properties` 始终保持Windows格式
- ✅ 不修改任何Gradle配置文件
- ✅ 保持项目对Android Studio完全兼容

### 2. MCP角色定位

MCP专注于：
- 智能代码生成
- 自动化重构
- 批量文件处理
- 代码分析和优化

Android Studio负责：
- 项目编译
- 运行和调试
- APK构建
- 真机测试

### 3. 错误处理流程

```
1. MCP生成代码
2. Android Studio自动编译
3. 如有错误，复制给MCP
4. MCP分析并修复
5. 重复直到成功
```

## 注意事项

### ⚠️ 不要做的事

1. **不要**修改`local.properties`为WSL格式
2. **不要**在MCP中强行编译Android项目
3. **不要**修改构建配置来适配MCP

### ✅ 应该做的事

1. **保持**项目配置为标准Android项目
2. **利用**MCP的代码生成能力
3. **使用**Android Studio进行编译验证

## 快速恢复指南

如果配置被意外修改：

```bash
# 恢复local.properties
cat > local.properties << EOF
sdk.dir=C:\\Users\\Hua\\AppData\\Local\\Android\\Sdk
EOF

# 恢复gradle.properties中的代理配置
# 移除或注释掉以下行：
# systemProp.http.proxyHost=127.0.0.1
# systemProp.http.proxyPort=7897
# systemProp.https.proxyHost=127.0.0.1
# systemProp.https.proxyPort=7897
```

## 总结

**核心原则**: 让每个工具做它擅长的事
- MCP = AI辅助编码
- Android Studio = 编译和运行

保持配置简单，避免复杂的环境适配，专注于提高开发效率。