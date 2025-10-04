# 🚀 快速继续工作 - CONTINUE HERE

> 当前优先任务：自动化记账（M1/M2已完成核心，待进行M3验收/补强）。

快速入口：`doc/开发进度/20250831-自动化记账-实施记录.md`

— 以下为历史版本迁移指南，保留以备后续使用 —

## 重启Claude后立即执行：

### 1️⃣ 验证MCP（10秒）
```
claude mcp list
```
应该看到：`kotlin-compiler: node /mnt/d/kotlin/mcp-kotlin-compiler/dist/server.js`

### 2️⃣ 测试MCP工作（30秒）
```
使用kotlin-compiler的check_gradle工具检查环境，projectPath是"."
```

### 3️⃣ 开始版本迁移（直接复制执行）

#### 第一个模块：shared-user
```bash
# 查看硬编码版本
rg '"[0-9]+\.[0-9]+\.[0-9]+"' shared/user/build.gradle.kts

# 编辑文件（使用版本映射表）
# 替换所有硬编码版本为libs.xxx引用

# 验证编译
使用kotlin-compiler的compile_kotlin工具编译模块，projectPath是"."，module是"shared-user"

# 提交
git add shared/user/build.gradle.kts
git commit -m "refactor: 迁移shared-user模块到版本目录"
```

## 📋 待迁移模块清单
- [ ] shared-user
- [ ] shared-sync  
- [ ] shared-backup
- [ ] shared-notification
- [ ] feature-plan（补充完成）

## 🗺️ 版本映射速查
```kotlin
// 最常用的替换
"34" → libs.versions.compileSdk.get().toInt()
"26" → libs.versions.minSdk.get().toInt()
"1.12.0" → libs.androidx.core.ktx
"2.7.0" → libs.androidx.lifecycle.*
"2024.02.00" → platform(libs.compose.bom)
```

## ⚠️ 记住：只改版本，不改逻辑！

---
详细文档：
- 执行手册：`doc/20250628-版本目录迁移执行手册.md`
- 完整映射：`doc/20250628-版本目录完整映射表.md`
- 会话恢复：`doc/20250628-会话恢复指南.md`
