# Repository Guidelines

## 项目结构与模块
- `app`: Android 应用模块（Compose UI、DI、构建配置）。源码位于 `app/src/main/java` 或 `app/src/main/kotlin`（推荐统一迁移至 `kotlin`），资源在 `app/src/main/res`，测试在 `app/src/test` 与 `app/src/androidTest`。
- `core`: 可复用基础能力——`core/common`、`core/ui`、`core/database`、`core/network`。
- `feature`: 纵向业务功能——`feature/todo`、`feature/habit`、`feature/ledger`、`feature/schedule`、`feature/plan`。
- `shared`: 跨功能服务——`shared/user`、`shared/sync`、`shared/notification`。
- 模块清单见 `settings.gradle.kts`。

## 构建、测试与运行
- `./gradlew assembleDebug`：构建 Debug APK（输出：`app/build/outputs/apk/debug/`）。Windows 可用 `build_debug.bat`。
- `./gradlew assembleRelease`：构建 Release APK；若存在 `keystore.properties` 将用于签名。Windows 可用 `build_release.bat`。
- `./gradlew test`：运行 JVM 单元测试（必要处启用 Robolectric）。
- `./gradlew connectedAndroidTest`：在设备/模拟器上运行仪器测试。
- `./gradlew lint`：执行 Android Lint 检查，提交前建议运行。
- `./gradlew :app:installDebug`：安装 Debug 构建到已连接设备。

## 代码风格与命名
- Kotlin 官方风格（`kotlin.code.style=official`），4 空格缩进；优先表达式函数体与不可变 `val`。
- 包名全小写点分，例如 `com.ccxiaoji.feature.plan`。
- 类/对象：PascalCase；函数/变量：camelCase；常量：UPPER_SNAKE_CASE。
- 资源命名：lowercase_underscore（如 `ic_add`、`color_primary`）。
- 分层建议：Compose 负责 UI，状态在 ViewModel（Hilt 注入），数据由 Repository/Room/Retrofit 提供。

## 测试规范
- 单元测试：JUnit4、MockK、Truth、Coroutines Test；放于 `src/test/kotlin`，命名如 `FooRepositoryTest`。
- 仪器/UI 测试：AndroidX Test、Espresso、Compose UI Test；放于 `src/androidTest`，命名如 `FastExcelIntegrationTest`、`FooScreenTest`。
- 运行：`./gradlew test`（单元）、`./gradlew connectedAndroidTest`（仪器）。关注仓库、ViewModel 与关键业务流程。

## 提交与合并请求
- 提交信息：遵循常见前缀 `feat:`、`fix:`、`refactor:`、`docs:`、`chore:`、`test:`，使用祈使句并尽量注明作用域。
- PR 要求：说明目的、关联 issue、测试计划（包含执行的命令）、UI 变更截图/GIF；涉及构建（Gradle、脚本、签名）需特别说明。

## 安全与配置
- 签名：勿提交真实密钥。参考 `keystore.properties.example` 本地创建 `keystore.properties`。
- 本地配置：SDK 路径置于 `local.properties`。代理/TLS 相关设置在 `gradle.properties`，生产环境避免放宽 TLS。
- 机密：严禁硬编码；通过 DataStore/安全存储与 DI 注入。
- 生产安全：`gradle.properties` 中的 TLS 放宽项仅限本地开发与排障，CI/Release 构建必须禁用（如 `jdk.internal.httpclient.disableHostnameVerification=true`、`com.sun.net.ssl.checkRevocation=false` 等）。

## Windows/编码（新增）
- 终端查看中文请使用 UTF-8：PowerShell 运行 `Get-Content -Encoding UTF8`，或执行 `chcp 65001` 切换代码页。
- 提交与编辑统一使用 UTF-8 编码（无 BOM）。

## Agent 指南
- 所有回答必须使用中文。

## 项目概览（简要）
- 应用：多模块生活管理应用（待办/习惯/记账/排班/计划）。
- 架构：模块化 + Clean Architecture + MVVM + Jetpack Compose(Material 3)。
- 模块分层：`api / data / domain / presentation`，依赖自上而下单向流动。

## 解决问题流程（必读）
- 多方案对比：输出 2–3 个可行方案，每个方案列出优点/缺点。
- 推荐与理由：明确给出推荐方案，并说明权衡依据（性能、维护性、复杂度、风险）。
- 适用范围：架构/跨模块变更/影响面大的改动需严格遵循；小型修复可简化但需给出思路说明。

## MCP / 构建工具指引
- MCP 工具（android-compiler）：提供编译与环境检查能力（Windows 已配置）。
  - `compile_kotlin`：编译项目或指定模块（`projectPath="."`，可选 `module`）。
  - `check_gradle`：检查 Gradle/Android 环境。
  - `prepare_android_build`：准备构建环境。
- 构建脚本与常用命令：
  - `build_debug.bat` / `./gradlew assembleDebug`：构建 Debug；输出 `app/build/outputs/apk/debug/`。
  - `build_release.bat` / `./gradlew assembleRelease`：构建 Release（如存在 `keystore.properties` 则签名）。
  - `install_apk.bat` / `./gradlew :app:installDebug`：安装 Debug 构建。
  - Android Studio：使用 Build Variants 在 debug/release 间切换。

## 语言与风格要求（强调）
- 回答、代码注释、提交信息、错误提示均使用中文。
- Compose 优先；状态驻留在 ViewModel（Hilt 注入），数据经 Repository/Room/Retrofit 提供。
- 保持 Kotlin 官方代码风格与现有命名规范（已在本文前述）。

## 开发环境基线
- Android Studio：2023.1.1+（Hedgehog）
- JDK：17（AGP≥8.2.1 可支持 Java 21）
- Gradle：8.9；AGP：推荐 8.3.0（最低 8.2.1）
- Kotlin：1.9.24
- SDK：Target 34，Min 26
- Compose：BOM 2024.10.00，Compiler 1.5.14

## 版本与插件管理规范（新增）
- 版本目录与来源：依赖版本统一在 `gradle/libs.versions.toml` 维护；插件版本统一在 `settings.gradle.kts` 的 `pluginManagement.plugins` 声明（版本目录的 `[plugins]` 仅保留插件 id 的 alias，不包含版本）。
- 命名建议：AndroidX 统一以 `androidx-` 前缀；Compose 统一以 `androidx-compose-` 前缀；避免同义别名（例如同时存在 `compose-bom` 与 `androidx-compose-bom`）。
- 插件引入：模块内优先使用 alias 方式（示例：`plugins { alias(libs.plugins.android.application) }`），版本由 settings 统一提供。历史存在 `id("com.android.*")` 的写法可保留但不推荐，新模块必须使用 alias。
- buildSrc 脚本：`buildSrc/src/main/kotlin/ccxiaoji.android.feature.gradle.kts`（如历史存在）视为示例/历史脚本，默认不启用；如需启用，必须改为从版本目录读取版本并与 settings 的 `pluginManagement.plugins` 对齐，确保不会向类路径提前注入“未知版本”的插件。新模块不建议直接依赖该脚本。

## 架构与依赖规则
- 依赖方向：`app → feature → shared → core`。
- 禁止：`feature → feature` 横向依赖；`core → feature/shared` 反向依赖。
- Feature 目录结构规范：
  - `api/`（对外 API）
  - `data/`（本地/远端与 Repository 实现）
  - `domain/`（模型与用例）
  - `presentation/`（Compose UI、ViewModel、模块内导航）

## 数据库与重要提醒
- Room 版本：2.6.1；数据库版本：v9（支持二级分类）。Schema 位于 `app/schemas/`。
- 命名差异：Schedule 模块使用 snake_case，其他模块使用 camelCase；通过 Mapper 保持一致性。
- 导入导出：已重构，支持选择性导入/导出、冲突处理与预览校验。
- 签名与版本并存：Debug 包名 `com.ccxiaoji.app.debug`，Release `com.ccxiaoji.app`，可并存安装。

## 常见问题与解决
- Java 21 与 AGP 8.1.x 兼容问题：升级至 AGP 8.2.1+。
- Hilt 重复绑定：去除重复 Provider 或合并绑定定义。
- 模块命名不一致：对 Schedule 采用映射/转换，避免跨层耦合。

## 文档与脚本组织
- 规则：
  - 文档集中 `doc/`；脚本集中 `scripts/`。
  - 避免在仓库根目录新增零散文档/脚本。
- 相关文档：
  - `Debug与Release版本切换指南.md`、`版本切换快速参考.md`
  - `doc/架构迁移计划与原则.md`、技术债评估/导入导出实现与修复等文档

## Demo 组件文档规范（新增）
- 组件目录：`feature/ledger/src/debug/.../demo/stylecatalog/components/`
- 文档路径：`doc/demo开发/可复用组件清单.md`
- 需要更新文档的时机：
  - 新建组件（新增 `.kt` 文件）
  - 组件公开 API 重要变更（签名/参数/功能）
  - 为现有组件新增重要特性
- 文档应包含（建议模板）：
  - 组件概述（功能与亮点）
  - 文件信息（路径与导入）
  - 函数签名（完整代码块）
  - 参数说明（名称/类型/是否必需/默认值/说明）
  - 使用示例（基础/高级/实际场景）
  - 注意事项（依赖/限制/性能）
  - 已知使用场景（引用页面清单）
  - 未来规划（迁移/改进）
- Agent 提示：当检测到上述目录组件新增或公开 API 变更时，需提醒/校验同步更新清单文档。
- 参考流程：创建组件 → 编译验证（如 `./gradlew :feature:ledger:compileDebugKotlin`）→ 更新组件清单 → 提交。

## 通知模块规范（新增）
- 访问方式：通过依赖注入获取 `shared/notification` 能力；不要直接依赖内部实现。
- 方法命名约定：
  - 即时通知：`sendXxxNotification()`
  - 定时提醒：`scheduleXxxReminder()`
  - 取消提醒：`cancelXxxReminder()`
- 场景映射（示例）：
  - 任务提醒：`sendTaskReminder()` / `scheduleTaskReminder()`
  - 习惯打卡：`sendHabitReminder()` / `scheduleDailyHabitReminder()`
  - 预算超支：`sendBudgetAlert()`
  - 信用卡还款：`sendCreditCardReminder()`
  - 通用通知：`sendGeneralNotification()`
- 权限检查最佳实践：统一使用 `NotificationAccessController` 检查与引导用户授权。

## 桌面小部件开发规范（新增）
- 标准流程：
  - 创建 `AppWidgetProvider` 并实现 `onUpdate`
  - 配置元数据文件：`res/xml/widget_info.xml`
  - 数据更新机制：
    - 周期任务：使用 `WorkManager`（如 `WidgetUpdateScheduler`）
    - 数据变更触发：发送刷新广播（如 `WidgetRefreshBroadcaster`）
    - 手动刷新：提供 UI 入口
  - 配置持久化：`SharedPreferences`（命名：`widget_prefs_${appWidgetId}`），在移除时清理
- 性能建议：
  - 避免在 `onUpdate` 中执行耗时操作；数据加载交由 `WorkManager`
  - 合理设置更新频率，避免过于频繁
- 兼容性：
  - 仅使用 `RemoteViews` 支持的组件
  - 注意不同屏幕尺寸与 Android 版本的展示与行为差异

## 数据库迁移管理规范（新增）
- 迁移文件位置：
  - 核心迁移（跨模块通用）：`core/database/migrations/`
  - 应用层迁移（应用特定修复/优化）：`app/data/local/migrations/`
- 命名规范：`Migration_<FROM>_<TO>.kt`（例如：`Migration_22_23.kt`）
- 开发流程：
  - 更新 `@Database(..., version = X)` 的版本号
  - 创建迁移对象（`Migration(FROM, TO)`）并实现 `migrate`
  - 在 `DatabaseMigrations.kt` 或 `AppMigrations.kt` 注册
  - 在 `DatabaseModule.kt` 通过 `.addMigrations(...)` 注入
  - 编写 `MigrationTest` 校验迁移前后数据完整性

## 协作与验证流程（补充）
- 代码修改流程：Agent 按需求修改 → 本地 `./gradlew :module:compileDebugKotlin` 验证 → Android Studio 同步/调试。
- 快速构建与安装：
  - 构建 Debug：`./gradlew assembleDebug` 或 `build_debug.bat`
  - 构建 Release：`./gradlew assembleRelease` 或 `build_release.bat`
  - 安装 Debug：`./gradlew :app:installDebug` 或 `install_apk.bat`
- 中国区构建优化：优先使用镜像仓库（Aliyun 等），官方源兜底；CI 推荐优先官方源，减少外部抖动影响。
