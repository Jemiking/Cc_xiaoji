# Android Studio 一键构建APK设置指南

## 🎯 目标

设置Android Studio，让你以后可以通过简单的操作快速构建正式版APK，无需重复配置密钥信息。

`★ 核心优势：一次配置，终身使用！再也不用每次都输入密钥信息！ ★`

---

## 📋 前置准备

✅ **已完成的配置**：
- 密钥库文件：`ccxiaoji_release.keystore`
- 签名配置：`keystore.properties`
- Gradle构建脚本：已正确配置自动签名

---

## 🚀 一键构建设置方案

### 方案1: Build Variants 快速构建（推荐）

这是最简单的方法，只需要点击几下就能生成APK。

#### **第1步：打开Build Variants面板**

1. **在Android Studio中**：
   - 打开你的项目 `D:\kotlin\Cc_xiaoji`
   - 点击左下角的 **"Build Variants"** 标签
   - 如果没有看到，去 `View` → `Tool Windows` → `Build Variants`

#### **第2步：选择构建变体**

在Build Variants面板中：
```
Module: app
Build Variant: release  ← 选择这个
```

#### **第3步：一键构建APK**

有3种快速方法：

**方法A：Gradle面板**
1. 右侧点击 **"Gradle"** 标签
2. 展开 `CC小记` → `app` → `Tasks` → `build`
3. **双击** `assembleRelease`
4. APK自动生成！

**方法B：Build菜单**
1. `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
2. 由于选择了release变体，自动构建正式版

**方法C：快捷键**
- **Windows**: `Ctrl + F9` (构建项目)
- 如果Build Variant选择了release，会自动构建正式版

---

### 方案2: Generate Signed APK（图形界面）

如果你喜欢图形界面操作，这个方法更直观。

#### **第1步：配置签名信息（只需要一次）**

1. `Build` → `Generate Signed Bundle / APK...`
2. 选择 **"APK"** → `Next`
3. 点击 **"Choose existing..."**
4. 浏览选择：`D:\kotlin\Cc_xiaoji\ccxiaoji_release.keystore`
5. 输入信息：
   ```
   Key store password: cfr930718
   Key alias: ccxiaoji_release
   Key password: cfr930718
   ```
6. **勾选** "Remember passwords" ← 重要！
7. `Next` → 选择 `release` → `Create`

#### **第2步：以后一键使用**

配置过一次后，以后只需要：
1. `Build` → `Generate Signed Bundle / APK...`
2. `APK` → `Next`
3. 密码已自动填入 → `Next`
4. `release` → `Create`

---

### 方案3: 自定义快捷构建

为终极便利性，创建自定义构建配置。

#### **配置Run Configuration**

1. 点击右上角运行按钮旁的下拉菜单
2. 选择 **"Edit Configurations..."**
3. 点击 **"+"** → **"Gradle"**
4. 配置如下：
   ```
   Name: 构建正式版APK
   Gradle project: CC小记
   Tasks: assembleRelease
   Arguments: --console=plain
   ```
5. `Apply` → `OK`

#### **一键执行**

以后只需要：
1. 在右上角下拉菜单选择 **"构建正式版APK"**
2. 点击 **绿色运行按钮** ▶️
3. APK自动生成完成！

---

## 📱 APK输出位置

无论使用哪种方法，生成的APK都在：
```
D:\kotlin\Cc_xiaoji\app\build\outputs\apk\release\app-release.apk
```

**快速查看方法**：
- Android Studio构建完成后，会弹出通知
- 点击通知中的 **"locate"** 或 **"Show in Explorer"**
- 自动打开文件夹

---

## 🛡️ 安全设置

### 保护密钥信息

1. **keystore.properties文件**：
   - 包含密钥密码，需要安全保存
   - 已加入`.gitignore`，不会被提交到Git

2. **Android Studio记住密码**：
   - 密码存储在Android Studio的安全存储中
   - 重装Android Studio需要重新输入

3. **备份提醒**：
   - 定期备份 `ccxiaoji_release.keystore`
   - 记录密钥密码：`cfr930718`

---

## ⚡ 快速操作流程

### 日常构建流程（3步搞定）

1. **打开项目** → 等待同步完成
2. **选择方案**：
   - 快速：Build Variants选择release → Ctrl+F9
   - 图形：Build → Generate Signed APK (密码已记住)
   - 自定义：运行按钮 → 构建正式版APK
3. **等待完成** → APK自动生成

### 性能优化建议

- **启用并行构建**：`File` → `Settings` → `Build` → `Compiler` → 勾选 "Compile independent modules in parallel"
- **增加内存**：`Help` → `Edit Custom VM Options` → 添加 `-Xmx4g`
- **开启离线模式**：`File` → `Settings` → `Build` → `Gradle` → 勾选 "Offline work"（网络稳定时）

---

## 🔧 高级配置

### 自动版本号递增

如果希望每次构建自动增加版本号：

1. 编辑 `app/build.gradle.kts`
2. 在 `defaultConfig` 块中添加：
   ```kotlin
   def versionPropsFile = file('version.properties')
   def versionCode = getVersionCode(versionPropsFile)
   
   defaultConfig {
       versionCode versionCode
       versionName "2.5.${versionCode - 250}"
   }
   ```

### 多渠道打包

如果需要为不同应用商店生成不同版本：

```kotlin
android {
    flavorDimensions += "store"
    productFlavors {
        create("huawei") {
            dimension = "store"
            versionNameSuffix = "-huawei"
            applicationIdSuffix = ".huawei"
        }
        create("xiaomi") {
            dimension = "store"
            versionNameSuffix = "-xiaomi"
            applicationIdSuffix = ".xiaomi"
        }
    }
}
```

### 构建后自动操作

在 `app/build.gradle.kts` 中添加：

```kotlin
tasks.register("copyApkToDesktop") {
    dependsOn("assembleRelease")
    doLast {
        copy {
            from("$buildDir/outputs/apk/release/")
            into("C:/Users/${System.getProperty("user.name")}/Desktop/")
            include("*.apk")
            rename("app-release.apk", "CC小记-v${android.defaultConfig.versionName}.apk")
        }
        println("APK已复制到桌面！")
    }
}
```

然后运行：`gradlew copyApkToDesktop`

---

## ❗ 常见问题解决

### Q1: Build Variants面板不显示release选项
**解决方案**：
1. `Build` → `Clean Project`
2. `Build` → `Rebuild Project`
3. 等待同步完成后重新查看

### Q2: 构建时提示"keystore not found"
**解决方案**：
1. 检查 `keystore.properties` 文件是否存在
2. 检查路径是否正确：`../ccxiaoji_release.keystore`
3. 检查密钥库文件是否在项目根目录

### Q3: Android Studio忘记了密码
**解决方案**：
1. 重新执行 Generate Signed APK流程
2. 重新输入密码并勾选"Remember passwords"

### Q4: 构建很慢
**解决方案**：
1. 启用Gradle缓存：`--build-cache`
2. 启用并行构建
3. 增加JVM内存分配

---

## 📋 验证清单

设置完成后，请验证以下项目：

- [ ] **Build Variants** 可以切换到release
- [ ] **Gradle面板** 可以看到assembleRelease任务
- [ ] **Generate Signed APK** 记住了密码
- [ ] **APK输出路径** 正确
- [ ] **签名验证** APK使用正式签名
- [ ] **功能测试** APK在设备上正常运行

---

## 🎉 完成！

设置完成后，你就可以享受一键构建的便利了！

**推荐使用顺序**：
1. **日常快速构建**：Build Variants + Ctrl+F9
2. **正式发布**：Generate Signed APK
3. **批量构建**：自定义Run Configuration

---

*最后更新：2025-08-17*  
*适用版本：Android Studio Hedgehog | 2023.1.1+*  
*项目：CC小记 v2.5.0*