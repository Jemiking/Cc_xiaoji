# 🔄 Debug与Release版本切换指南

## 🎯 切换场景

在开发过程中，你可能需要在以下情况间切换：

- **📱 正式发布** → 使用Release版本（已签名、已优化）
- **🐛 问题调试** → 使用Debug版本（保留调试信息）
- **🧪 功能测试** → 使用Debug版本（快速迭代）
- **📊 性能测试** → 使用Release版本（真实性能）

---

## 🚀 方案对比

| 方案 | 速度 | 便利性 | 适用场景 | 备注 |
|------|------|--------|----------|------|
| Build Variants | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 频繁切换 | Android Studio内一键切换 |
| 双脚本构建 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | 命令行用户 | 可同时保留两个版本 |
| Run Configuration | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ | 自动化流程 | 预设好的一键运行 |
| Gradle命令 | ⭐⭐⭐⭐ | ⭐⭐ | CI/CD | 脚本化构建 |

---

## 🔧 方案一：Build Variants切换（推荐）

### ⚡ 快速切换步骤

1. **打开Build Variants面板**
   ```
   Android Studio → 左下角 "Build Variants" 标签
   如果没有：View → Tool Windows → Build Variants
   ```

2. **切换构建变体**
   ```
   Module: app
   Build Variant: debug / release  ← 点击下拉选择
   ```

3. **构建对应版本**
   - **快捷键**: `Ctrl + F9`
   - **菜单**: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`

### 📁 输出位置

- **Debug版本**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release版本**: `app/build/outputs/apk/release/app-release.apk`

---

## 🔧 方案二：双脚本构建（已创建）

### 📝 可用脚本

1. **构建Debug版本**
   ```bash
   # Windows
   build_debug.bat
   
   # 或直接命令
   gradlew.bat assembleDebug
   ```

2. **构建Release版本**
   ```bash
   # Windows  
   build_release.bat
   
   # 或直接命令
   gradlew.bat assembleRelease
   ```

### ✨ 脚本优势

- **并行存在**: 两个版本可以同时存在
- **独立构建**: 不影响Android Studio设置
- **自动化**: 可集成到CI/CD流程

---

## 🔧 方案三：Run Configuration设置

### 创建Debug运行配置

1. **Android Studio** → 右上角下拉菜单 → **Edit Configurations...**

2. **添加Gradle配置**
   ```
   点击 "+" → Gradle
   
   Name: 构建Debug版APK
   Gradle project: CC小记
   Tasks: assembleDebug
   Arguments: --console=plain
   ```

3. **添加Release运行配置**
   ```
   点击 "+" → Gradle
   
   Name: 构建Release版APK
   Gradle project: CC小记  
   Tasks: assembleRelease
   Arguments: --console=plain
   ```

### 🎯 一键执行

以后只需要：
1. **右上角下拉菜单** → 选择对应配置
2. **点击绿色运行按钮** ▶️

---

## 🛠️ Debug版本优化配置

让我检查和优化debug配置，确保最佳调试体验：

### 当前Debug配置（app/build.gradle.kts）

```kotlin
debug {
    isMinifyEnabled = false      // ✅ 不混淆代码
    isShrinkResources = false   // ✅ 不压缩资源  
    isDebuggable = true         // ✅ 启用调试
}
```

### 🔍 推荐的Debug增强配置

可以在build.gradle.kts中添加：

```kotlin
debug {
    isMinifyEnabled = false
    isShrinkResources = false
    isDebuggable = true
    
    // 调试增强配置
    applicationIdSuffix = \".debug\"     // 允许同时安装
    versionNameSuffix = \"-debug\"       // 版本标识
    
    // 构建性能优化
    isJniDebuggable = false             // 减少构建时间
    isRenderscriptDebuggable = false    // 减少构建时间
    
    // 调试信息
    buildConfigField(\"String\", \"BUILD_TYPE\", \"\\\"DEBUG\\\"\")
    buildConfigField(\"boolean\", \"LOGGING_ENABLED\", \"true\")
}
```

---

## 📱 安装和卸载管理

### 版本冲突问题

**问题**: Release和Debug版本签名不同，无法直接覆盖安装

**解决方案**：

1. **方案A: 先卸载再安装**
   ```bash
   adb uninstall com.ccxiaoji.app
   adb install app-debug.apk
   ```

2. **方案B: 使用不同包名（推荐）**
   - Debug版本: `com.ccxiaoji.app.debug`
   - Release版本: `com.ccxiaoji.app`
   - 两个版本可以同时存在

### 🔄 快速安装脚本

创建安装脚本：

```batch
@echo off
echo 选择要安装的版本：
echo 1. Debug版本
echo 2. Release版本
echo 3. 卸载所有版本

set /p choice=请输入选择(1-3): 

if %choice%==1 (
    echo 安装Debug版本...
    adb install -r app\\build\\outputs\\apk\\debug\\app-debug.apk
) else if %choice%==2 (
    echo 安装Release版本...  
    adb install -r app\\build\\outputs\\apk\\release\\app-release.apk
) else if %choice%==3 (
    echo 卸载所有版本...
    adb uninstall com.ccxiaoji.app
    adb uninstall com.ccxiaoji.app.debug
)
```

---

## 🧪 调试最佳实践

### Debug版本调试技巧

1. **日志输出**
   ```kotlin
   // 在debug版本中启用详细日志
   if (BuildConfig.DEBUG) {
       Log.d(TAG, "详细调试信息")
   }
   ```

2. **调试工具启用**
   ```kotlin
   // debug版本启用调试工具
   if (BuildConfig.DEBUG) {
       // 启用Flipper、LeakCanary等调试工具
   }
   ```

3. **网络配置**
   ```kotlin
   // debug版本使用测试服务器
   val baseUrl = if (BuildConfig.DEBUG) {
       "https://test-api.ccxiaoji.com/"
   } else {
       "https://api.ccxiaoji.com/"
   }
   ```

### Release版本测试要点

1. **性能测试**: 混淆后的真实性能
2. **兼容性测试**: 不同设备上的表现
3. **安全测试**: 签名和权限验证
4. **功能完整性**: 确保所有功能正常

---

## ⚠️ 注意事项

### 🔐 签名差异

- **Debug**: 使用Android默认调试签名
- **Release**: 使用自定义签名（ccxiaoji_release.keystore）

### 📦 APK差异

| 特性 | Debug版本 | Release版本 |
|------|-----------|-------------|
| 代码混淆 | ❌ 不混淆 | ✅ 已混淆 |
| 资源压缩 | ❌ 不压缩 | ✅ 已压缩 |
| 调试信息 | ✅ 完整保留 | ❌ 已移除 |
| APK大小 | 🔴 较大 | 🟢 较小 |
| 构建速度 | 🟢 快速 | 🔴 较慢 |
| 调试支持 | 🟢 完整 | 🔴 受限 |

### 🐛 常见问题

1. **Q: 切换后无法安装？**
   ```
   A: 签名不同，需要先卸载旧版本
   ```

2. **Q: Debug版本性能差？**
   ```
   A: 正常现象，包含调试信息和未优化代码
   ```

3. **Q: Release版本无法调试？**
   ```
   A: 代码已混淆，使用debug版本进行调试
   ```

---

## 🎯 推荐工作流程

### 日常开发流程

1. **功能开发**: 使用Debug版本
   ```
   Build Variants → debug → Ctrl+F9
   ```

2. **问题调试**: 使用Debug版本
   ```
   添加断点 → Debug运行 → 逐步调试
   ```

3. **性能测试**: 使用Release版本
   ```
   Build Variants → release → 性能分析
   ```

4. **发布准备**: 使用Release版本
   ```
   build_release.bat → 测试 → 发布
   ```

### 团队协作建议

1. **版本标识**: Debug和Release使用不同图标
2. **测试覆盖**: 两个版本都要测试
3. **问题复现**: 先用Debug调试，再用Release验证
4. **发布检查**: Release版本功能完整性检查

---

## 📋 快速参考

### 🔄 切换命令速查

```bash
# 构建Debug版本
gradlew assembleDebug

# 构建Release版本  
gradlew assembleRelease

# 安装Debug版本
adb install app/build/outputs/apk/debug/app-debug.apk

# 安装Release版本
adb install app/build/outputs/apk/release/app-release.apk

# 卸载应用
adb uninstall com.ccxiaoji.app
```

### ⌨️ Android Studio快捷键

| 操作 | 快捷键 |
|------|--------|
| 构建项目 | `Ctrl + F9` |
| 清理项目 | `Build → Clean Project` |
| 重新构建 | `Build → Rebuild Project` |
| 运行配置 | `Alt + Shift + F10` |

---

*最后更新：2025-08-17*  
*适用版本：CC小记 v2.5.0*  
*配置状态：已完成Release签名配置*