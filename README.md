# CC小记 - 综合生活管理应用

CC小记是一款基于Android的综合生活管理应用，帮助用户管理日常生活的方方面面。

## 功能特点

### 📊 记账模块
- 收支记录管理
- 多账户支持
- 自定义分类
- 预算管理
- 统计分析报表

### ✅ 待办模块
- 任务创建与管理
- 优先级设置
- 任务提醒
- 完成进度追踪

### 🎯 习惯模块
- 习惯养成追踪
- 打卡记录
- 连续天数统计
- 成就系统

### 💾 数据管理
- 本地数据备份
- 云端同步
- 数据导入导出
- 批量操作

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose
- **架构**: MVVM + Clean Architecture
- **数据库**: Room
- **依赖注入**: Hilt
- **异步处理**: Coroutines + Flow
- **网络请求**: Retrofit
- **工作调度**: WorkManager

## 开发环境

- Android Studio Hedgehog | 2023.1.1 或更高版本
- JDK 17
- Gradle 8.4
- Android SDK 34

## 构建说明

1. 克隆项目
```bash
git clone https://github.com/Jemiking/Cc_xiaoji.git
```

2. 使用Android Studio打开项目

3. 同步Gradle依赖

4. 运行应用

### 签名配置与构建说明

Release 构建支持两种安全方式配置签名：

- 环境变量（推荐在 CI/临时构建使用）
  - `KEYSTORE_FILE`：签名文件路径（可为绝对路径或相对仓库根路径）
  - `KEYSTORE_PASSWORD`：密钥库密码
  - `KEY_ALIAS`：密钥别名
  - `KEY_PASSWORD`：密钥密码

- 本地忽略文件（适合个人开发机）
  - 将 `keystore.properties.example` 复制为 `tools/secrets/keystore.properties`
  - 将签名文件放在 `tools/secrets/ccxiaoji_release.keystore`
  - 编辑 `tools/secrets/keystore.properties`：
    - `storeFile=tools/secrets/ccxiaoji_release.keystore`
    - `storePassword=...`、`keyAlias=...`、`keyPassword=...`
  - 注意：`tools/secrets/` 和 `keystore.properties`、`*.keystore` 已在 `.gitignore` 中忽略，请勿提交。

构建命令：

- Debug：`scripts/windows/build_debug.bat`
- Release：`scripts/windows/build_release.bat`
  - 若未检测到完整签名配置，将使用 Debug 签名构建 Release（仅供内部验证，不能上架）。

实现说明：`app/build.gradle.kts` 会自动优先读取环境变量，然后读取 `tools/secrets/keystore.properties`（若存在）。

### Excel功能构建说明

本项目使用 `cn.idev.excel:fastexcel` 处理Excel导入导出功能，采用了POI包名重定位技术来避免依赖冲突：

- **自动构建**：构建过程会自动执行POI重定位，无需额外操作
- **Shadow任务**：`shadowPoi` 任务会在 `preBuild` 时自动执行
- **重定位JAR**：生成的POI JAR位于 `app/build/relocated/` 目录

如需手动执行POI重定位：
```bash
./gradlew shadowPoi
```

详细技术方案请参考：[FastExcel-POI重定位方案文档](doc/FastExcel-POI重定位方案文档.md)

## 项目结构

```
app/
├── data/           # 数据层
│   ├── local/      # 本地数据库
│   ├── remote/     # 远程API
│   └── repository/ # 数据仓库
├── domain/         # 领域层
│   └── model/      # 业务模型
├── presentation/   # 表现层
│   ├── ui/         # UI组件
│   └── viewmodel/  # 视图模型
└── di/             # 依赖注入
```

## 目录结构一览（标准化后）

- `app/`：Android 应用模块（Compose UI、DI、构建配置）
- `core/`：通用能力（common/ui/database/network）
- `feature/`：业务功能模块（todo/habit/ledger/schedule/plan）
- `shared/`：跨功能服务（user/sync/notification）
- `doc/`：文档中心（指南、报告、版本记录）；根目录仅保留本 README
  - `doc/guides/`、`doc/guides/build/`、`doc/guides/mcp/`
  - `doc/reports/`、`doc/release-notes/`
- `scripts/`：脚本入口；Windows 批处理在 `scripts/windows/`
- `tools/`：外部工具与集成（如 MCP 工具在 `tools/mcp/`）
- `testdata/`：样例与测试数据（导入/导出样例、边界用例）
- `logs/`、`out/`：构建与运行输出（已加入 .gitignore）

## 贡献指南

欢迎提交Issue和Pull Request！

### 提交前检查（必读）

- 本地执行 `./gradlew check` 并通过（已包含“重复枚举名校验”）：防止在 `feature/ledger` 的 `data/local/entity` 与 `domain` 下出现同名枚举造成歧义。
- 如涉及数据库：请同步更新 Room Schema（`app/schemas`）与 Migration，并补充必要测试。
- 如涉及构建/脚本/签名：在 PR 中“特别说明”区写明改动与验证方式。
- 如涉及 UI 变更：请附上截图/GIF。

详细规则见：`doc/代码约束-重复枚举检测.md`；PR 模板见：`.github/PULL_REQUEST_TEMPLATE.md`。

## 许可证

[待定]

---
🤖 使用 [Claude Code](https://claude.ai/code) 辅助开发
