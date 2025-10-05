# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview
CC小记 (CC Xiaoji) is a **Life Management Companion App** that integrates multiple life management modules including task management, habit tracking, and personal finance. Built with modern Android architecture and designed for extensibility.

### Architecture Status
✅ **Successfully migrated from monolithic to modular architecture**
- **Modern Architecture**: Domain-driven modular design with Clean Architecture + MVVM
- **Latest UI Technology**: 100% Jetpack Compose with Material 3 Design System
- **High Performance**: 56% compilation time improvement through modularization
- **Developer Friendly**: Complete documentation system and clear architecture guidelines
- **China-Optimized**: Aliyun Maven mirrors for faster builds in China

### Module Structure
- **Completed Modules**: 
  - ✅ 4 Core modules (common, ui, database, network)
  - ✅ 3 Shared modules (user, sync, notification)
  - ✅ 5 Feature modules (todo, habit, ledger, schedule, plan)
  - ✅ 1 App module (streamlined as application shell)

## Development Workflow

### Code Modification Process
1. Claude Code makes the requested code changes
2. Claude Code can use MCP server for compilation verification
3. Developer can also manually compile in Android Studio
4. For complex build issues, use Android Studio for debugging

### Demo Component Documentation (2025-09-30新增)
**所有在demo环境中创建的可复用组件都必须记录到组件文档中**

#### 文档维护规则
当在以下路径创建或修改组件时，必须同步更新组件清单文档：
- **组件路径**: `feature/ledger/src/debug/.../demo/stylecatalog/components/`
- **文档路径**: `doc/demo开发/可复用组件清单.md`

#### 必须记录的时机
1. **创建新组件**: 在 `components/` 目录下创建任何新的 `.kt` 文件
2. **重要修改**: 组件的函数签名、参数、功能发生重大变化
3. **新增功能**: 为现有组件添加重要新特性

#### 记录内容要求
每个组件必须包含以下信息：
```markdown
## X️⃣ 组件名称

### 📝 组件概述
- 功能描述
- 核心亮点（使用 emoji 标记）

### 📂 文件信息
- 路径
- 导入语句

### 🔧 函数签名
- 完整的函数签名代码块

### 📋 参数说明
- 参数表格（参数名、类型、必需、默认值、说明）

### 💡 使用示例
- 基础用法
- 高级用法
- 实际应用案例

### ⚠️ 注意事项
- 依赖库
- 限制说明
- 性能考虑

### 🔮 已知使用场景
- 列出所有使用该组件的页面

### 🚀 未来规划
- 迁移计划
- 可能的改进
```

#### 自动提醒机制
Claude Code在以下情况会主动提醒更新文档：
- ✅ 创建新组件文件后
- ✅ 修改组件的公开API后
- ✅ 在其他页面中使用组件后

#### 示例流程
```
1. 创建组件: BookSelectionBottomSheet.kt
2. 实现功能: 账本选择底部弹窗
3. 编译验证: ./gradlew :feature:ledger:compileDebugKotlin
4. ⭐ 更新文档: 在"可复用组件清单.md"中添加完整记录
5. 提交代码: 包含组件和文档的更新
```

#### 文档位置
- **主文档**: `doc/demo开发/可复用组件清单.md`
- **访问方式**:
  - 本地查看: 使用Markdown阅读器
  - 团队共享: 通过版本控制系统同步

### Debug与Release版本切换 (2025-08-17新增)
**已配置完整的版本切换系统，支持调试和发布版本快速切换**

#### 可用切换方法
1. **Android Studio Build Variants** (推荐)
   - 左下角Build Variants面板 → 选择debug/release → Ctrl+F9
   
2. **命令行脚本**
   - `build_debug.bat` - 构建Debug版本
   - `build_release.bat` - 构建Release版本
   - `install_apk.bat` - 智能APK安装工具

3. **版本特性**
   - Debug版本: `com.ccxiaoji.app.debug` (可与正式版并存)
   - Release版本: `com.ccxiaoji.app` (正式签名版本)
   - 输出位置: `app/build/outputs/apk/debug(release)/`

#### 使用场景
- **问题调试**: 使用Debug版本 (保留调试信息，未混淆)
- **性能测试**: 使用Release版本 (代码优化，真实性能)
- **发布准备**: 使用Release版本 (正式签名，生产就绪)

#### 相关文档
- `Debug与Release版本切换指南.md` - 详细操作指南
- `版本切换快速参考.md` - 快速参考卡片

### Problem-Solving Approach
**Before implementing any solution, Claude Code must:**

1. **Present multiple solution options** (typically 2-3 different approaches)
2. **Analyze pros and cons for each solution:**
   - **优点 (Pros)**: Performance impact, maintainability, code simplicity
   - **缺点 (Cons)**: Implementation complexity, potential risks, limitations
3. **Provide a clear recommendation with reasoning**

**Example format:**
```
问题：[描述具体问题]

方案一：[方案名称]
- 优点：
  • [优点1]
  • [优点2]
- 缺点：
  • [缺点1]
  • [缺点2]

推荐方案：方案X
理由：[详细解释为什么推荐这个方案]
```

### Shared Modules Usage Guidelines (2025-10-04新增)
**正确使用共享模块，特别是notification模块，确保模块间协作规范**

#### Notification模块使用规范
当功能模块需要发送通知时，必须遵循以下规范：

1. **依赖注入方式**
   ```kotlin
   @Inject
   lateinit var notificationApi: NotificationApi
   ```
   - ✅ 使用Hilt依赖注入NotificationApi
   - ❌ 不要直接创建NotificationManager实例

2. **通知发送流程**
   ```kotlin
   // 1. 检查通知权限（如需要）
   if (notificationAccessController.hasNotificationPermission()) {
       // 2. 调用对应的通知方法
       notificationApi.sendTaskReminder(taskId, title, dueTime)
   }
   ```

3. **通知调度规范**
   - **即时通知**: 使用 `sendXxxNotification()` 方法
   - **定时通知**: 使用 `scheduleXxxReminder()` 方法
   - **取消通知**: 使用 `cancelXxxReminder()` 方法

4. **通知类型选择**
   - 任务提醒: `sendTaskReminder()` / `scheduleTaskReminder()`
   - 习惯打卡: `sendHabitReminder()` / `scheduleDailyHabitReminder()`
   - 预算超支: `sendBudgetAlert()`
   - 信用卡还款: `sendCreditCardReminder()`
   - 通用通知: `sendGeneralNotification()`

5. **权限检查最佳实践**
   ```kotlin
   // 使用NotificationAccessController检查权限
   if (!notificationAccessController.hasNotificationPermission()) {
       // 引导用户开启通知权限
       notificationAccessController.requestNotificationPermission(context)
   }
   ```

#### 其他Shared模块规范
- **User模块**: 提供用户信息和认证，通过UserRepository访问
- **Sync模块**: 提供数据同步能力，通过SyncManager访问
- **通用原则**:
  - 功能模块通过API接口访问shared模块
  - 不直接访问shared模块的内部实现
  - 使用依赖注入获取shared模块服务

### Desktop Widget Development Guidelines (2025-10-04新增)
**桌面小部件开发标准流程和最佳实践**

#### Widget开发标准流程
1. **创建Widget Provider**
   ```kotlin
   class MyWidgetProvider : AppWidgetProvider() {
       override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
           // 更新小部件UI
       }
   }
   ```

2. **配置Widget元数据** (`res/xml/widget_info.xml`)
   ```xml
   <appwidget-provider
       android:minWidth="250dp"
       android:minHeight="110dp"
       android:updatePeriodMillis="1800000"
       android:initialLayout="@layout/widget_layout"
       android:configure="com.example.ConfigActivity"
       android:previewImage="@drawable/widget_preview" />
   ```

3. **数据更新机制**
   - **定时更新**: 使用 `WidgetUpdateScheduler` 配置WorkManager周期任务
   - **数据变更触发**: 使用 `WidgetRefreshBroadcaster` 发送刷新广播
   - **手动刷新**: 提供用户手动刷新按钮

4. **配置持久化**
   - 使用SharedPreferences存储Widget配置
   - 命名规范: `widget_prefs_${appWidgetId}`
   - 在Widget删除时清理配置（使用CleanupWorker）

5. **性能优化**
   - 避免在onUpdate中执行耗时操作
   - 使用WorkManager处理数据加载
   - 合理设置更新频率（避免过于频繁）

6. **Widget配置界面**
   - 创建ConfigActivity继承AppWidgetConfigActivity
   - 配置完成后调用 `setResult(RESULT_OK, resultValue)`
   - 提供预览功能（Debug版本可用PreviewActivity）

#### Widget开发注意事项
- ⚠️ Widget布局不支持所有View组件，参考RemoteViews支持列表
- ⚠️ 数据更新延迟正常，Android系统有更新频率限制
- ⚠️ 测试时注意不同屏幕尺寸和Android版本兼容性

### Database Migration Management (2025-10-04新增)
**数据库迁移文件的组织和管理规范**

#### 迁移文件位置规则
项目中有两个迁移文件位置，使用场景如下：

1. **core/database/migrations/** - 核心迁移
   - **使用场景**: 涉及多个模块的通用迁移
   - **文件**: `Migration_X_Y.kt` 和 `DatabaseMigrations.kt`
   - **示例**: v1-v19的核心表结构迁移

2. **app/data/local/migrations/** - 应用层迁移
   - **使用场景**: 应用特定的数据修复和优化
   - **文件**: `AppMigrations.kt`
   - **示例**: v19-v23的数据完善和索引调整

#### 迁移文件命名规范
```
Migration_<FROM>_<TO>.kt
```
- FROM: 起始版本号
- TO: 目标版本号
- 示例: `Migration_22_23.kt`

#### 迁移开发流程
1. **更新数据库版本号**
   ```kotlin
   @Database(entities = [...], version = 24)
   ```

2. **创建迁移文件**
   ```kotlin
   val MIGRATION_23_24: Migration = object : Migration(23, 24) {
       override fun migrate(db: SupportSQLiteDatabase) {
           // 执行迁移SQL
           db.execSQL("ALTER TABLE ...")
       }
   }
   ```

3. **注册迁移**
   - 在 `DatabaseMigrations.kt` 或 `AppMigrations.kt` 中注册
   - 在 `DatabaseModule.kt` 中添加到 `.addMigrations()` 调用

4. **测试迁移**
   - 编写 `MigrationTest` 测试用例
   - 验证迁移前后数据完整性
   - 测试降级场景（如需要）

#### 迁移最佳实践
- ✅ 每次迁移只做一件事，保持迁移原子性
- ✅ 提供详细注释说明迁移目的
- ✅ 使用 `IF NOT EXISTS` 和 `IF EXISTS` 确保幂等性
- ✅ 备份重要数据，特别是表结构调整时
- ❌ 不要在迁移中执行复杂的业务逻辑
- ❌ 不要依赖实体类（迁移应该自包含）

## MCP Server Configuration
**Android Compiler MCP server is configured for automatic compilation verification.**

### MCP Server Details
- **Server Name**: android-compiler
- **Version**: 2.0.0 (Optimized)
- **Purpose**: Provides automatic Kotlin/Android compilation capabilities
- **Location**: `android-compiler-mcp-windows/`

### Available MCP Tools
1. **compile_kotlin** - Compiles the Android Kotlin project
2. **check_gradle** - Checks Gradle and Android environment
3. **prepare_android_build** - Prepare Android build environment

### Usage Examples
```
# Check environment
使用check_gradle工具检查环境，projectPath是"."

# Compile entire project
使用compile_kotlin工具编译项目，projectPath是"."

# Compile specific module
使用compile_kotlin工具编译feature-ledger模块，projectPath是"."，module是"feature-ledger"
```

## Language Requirement
**All responses from Claude Code should be in Chinese (中文).** This includes:
- Code comments and documentation
- Explanations and descriptions
- Error messages and feedback
- Communication with the developer

## Development Environment
- **Android Studio**: Hedgehog | 2023.1.1 or higher
- **JDK**: 17 minimum (Java 21 supported with AGP 8.2.1+)
- **Gradle**: 8.9
- **Android SDK**: 34
- **MinSdk**: 26 (Android 8.0)
- **TargetSdk**: 34 (Android 14)

## Database Management
- **Room database version**: 23 (Current - Enhanced notification and reminder system)
- **Tables**: 28 tables total
- **Distribution**:
  - Ledger: 11 tables (including cards management)
  - Schedule: 4 tables
  - Plan: 3 tables
  - Habit: 2 tables
  - Todo: 1 table
  - Auto Ledger: 2 tables
  - Others: 5 tables
- **Version History**:
  - v5: Schedule module integration
  - v6: Plan module integration
  - v7: Credit card fields
  - v8: SavingsGoal userId field
  - v9: Two-level category support (2025-08-15)
  - v10: Transaction time and location support (2025-08-19)
  - v11: Ledger notebook support (2025-08-20)
  - v12: Fixed transactions table foreign keys and indexes (2025-08-20)
  - v13: Advanced ledger linking system (2025-08-24)
  - v14: Automated ledger tracking enhancements (2025-08-24)
  - v15: Transaction relationship management (2025-08-24)
  - v16: Transfer functionality support (2025-08-24)
  - v17: Cards table for bank card management (2025-09-26)
  - v18: Card holderName and institution fields (2025-09-26)
  - v19: Schedule sync_status type optimization (2025-09-26)
  - v20: Category data migration and index adjustment (2025-09-28)
  - v21: Category isHidden field for transfer categories (2025-09-28)
  - v22: Task and Habit reminder configuration fields (2025-10-04)
  - v23: Task fixed-time reminder support (2025-10-04)
- **Architecture Note**: Schedule module uses snake_case, others use camelCase
- **Schema location**: `app/schemas/`

## Architecture Overview

### Domain-Based Modular Architecture

#### Module Categories
1. **App Module** - Application shell and navigation orchestration
2. **Core Modules** - Infrastructure and shared foundations
3. **Shared Modules** - Cross-domain business capabilities
4. **Feature Modules** - Domain-specific business features

#### Clean Architecture + MVVM Pattern
Each module follows a three-layer architecture:

1. **Data Layer** (`data/`)
   - Local: Room database with DAOs and entities
   - Remote: Retrofit APIs for sync functionality
   - Repository: Implements data access abstraction

2. **Domain Layer** (`domain/`)
   - Model: Business models that are UI-independent
   - UseCase: Business logic encapsulation

3. **Presentation Layer** (`presentation/`)
   - UI: Jetpack Compose screens and components
   - ViewModel: State holders using Hilt injection
   - Navigation: Module-specific navigation

### Dependency Rules
```
✅ Allowed:
app → feature → shared → core

❌ Forbidden:
feature → feature (no horizontal dependencies)
core → feature (no reverse dependencies)
core → shared (no reverse dependencies)
```

### Module Structure Standard
```
feature-[name]/
├── api/           # Public API for other modules
├── data/          # Data layer implementation
│   ├── local/
│   │   ├── dao/
│   │   └── entity/
│   └── repository/
├── domain/        # Business logic
│   ├── model/
│   └── usecase/
└── presentation/  # UI layer
    ├── screen/
    ├── component/
    └── viewmodel/
```

## Technology Stack

### Core Technologies
- **Language**: Kotlin 1.9.24
- **Build System**: Gradle 8.9 + Android Gradle Plugin 8.3.0
- **Compose BOM**: 2024.10.00
- **Compose Compiler**: 1.5.14

### Key Libraries
- **Dependency Injection**: Hilt 2.51.1
- **Database**: Room 2.6.1
- **Networking**: Retrofit 2.9.0, OkHttp 4.12.0
- **Async**: Coroutines 1.7.3, Flow
- **Background**: WorkManager 2.9.0
- **Navigation**: Navigation Compose 2.7.6
- **DateTime**: Kotlinx DateTime 0.5.0

## Current Project Status (2025-10-05)

### ✅ Completed Features
1. **Architecture Migration**: 100% complete with 13 modules (including notification module)
2. **Navigation**: 2 active bottom navigation items (Home, Profile)
   - Other modules (Ledger, Todo, Habit, Schedule) are commented but ready
3. **Database**: Version 23 with enhanced notification and reminder system
4. **Core Features**: Todo, Habit, Ledger all functional
5. **Schedule Module**: Successfully integrated
6. **Plan Module**: Successfully integrated with tree structure
7. **Ledger Notebook System**: Complete ledger book management functionality
8. **Unified Account Asset Management**: Tab-based dashboard integrating asset overview and account management
9. **Transfer Functionality**: Complete transfer transaction system with linked records
10. **Auto Ledger System**: Complete notification-based automatic bookkeeping infrastructure
11. **Bank Card Management**: Complete card management with holder and institution info
12. **Notification System**: Unified notification module for tasks, habits, budgets, and credit cards
13. **Desktop Widgets**: Ledger widget and countdown widget for home screen
14. **Enhanced Reminders**: Personalized reminder configuration for tasks and habits
15. **Test Framework**: JUnit + MockK + Truth configured

### 📊 Technical Debt Status
**Current Status**: 持续清理中，健康度约82%

**Key Metrics**:
- Test coverage: ~25-30% (target: 45%)
- Architecture consistency: ~85% (target: 95%)
- Unfinished items:
  - Version catalog: 7 modules still using hardcoded versions
  - LedgerViewModel: 320 lines (target: 200 lines)
  - Internationalization: Ledger module has 41 files pending

### ✅ Recent Updates

#### 二级分类系统完成（2025-08-15）
**进度：Phase 7/7 完成 ✅**
- ✅ Phase 1: 数据模型优化（数据库v8→v9）
- ✅ Phase 2: Repository和业务逻辑层（11个新方法+5个UseCase）
- ✅ Phase 3: ViewModel层改造（4个ViewModel支持二级分类）
- ✅ Phase 4: UI组件开发（CategoryPicker、CategoryPathDisplay、CategoryEditDialog）
- ✅ Phase 5: 默认分类初始化（80+个预设分类）
- ✅ Phase 6: 导入导出适配（支持钱迹数据映射、CSV导入导出）
- ✅ Phase 7: 测试优化（单元测试、性能缓存、代码清理）

**新增功能**:
- 完整的二级分类系统（父分类-子分类结构）
- 智能分类选择器with搜索功能
- 默认分类自动初始化（新用户友好）
- 钱迹数据智能映射到二级分类
- 分类缓存机制提升性能
- 全面的单元测试覆盖

#### 记账功能开发完成（2024-06-22）
- ✅ Credit card management
- ✅ Asset overview page
- ✅ Ledger settings page
- ✅ Batch operations
- ✅ Advanced filtering
- ✅ Performance optimization

#### 数据导入导出重构（2025-08-14）
- ✅ Old system completely removed
- ✅ New modular architecture implemented
- ✅ CSV export with 100% data coverage
- ✅ Support for 9 data types

#### 数据导入修复（2025-08-15）
**Fixed Issues**:
1. **File selector compatibility**
   - Changed from `GetContent()` to `OpenDocument()`
   - Support multiple MIME types
   - Added "Browse all files" fallback

2. **Data mapping issue**
   - Fixed Skip handling to maintain name-to-ID mapping
   - Modified `ResolveResult.Skip` to include existing entity
   - Ensures dependent data imports correctly

**Import/Export Features**:
- ✅ CSV format (single file)
- ✅ Selective export/import
- ✅ Conflict resolution (Skip, Merge, Rename, Overwrite)
- ✅ Preview and validation
- ✅ Proper reference handling

**Access Path**: 
- Export: 主界面 → 个人 → 记账设置 → 数据导出
- Import: 主界面 → 个人 → 记账设置 → 数据导入

#### APK签名和版本切换配置（2025-08-17）
**完成了完整的Release签名配置和Debug/Release版本切换系统**

**APK签名配置**:
1. **正式签名配置**
   - 密钥库文件: `ccxiaoji_release.keystore`
   - 配置文件: `keystore.properties` 
   - Gradle自动签名: 已配置完成

2. **Android Studio一键构建**
   - Build Variants快速切换
   - Generate Signed APK记住密码
   - 自定义Run Configuration
   - 详细设置指南完成

**版本并存配置**:
- **Debug版本**: `com.ccxiaoji.app.debug` (调试优化)
- **Release版本**: `com.ccxiaoji.app` (正式签名)
- **同时安装**: 两个版本可在同一设备并存
- **智能切换**: 提供多种切换方法和安装工具

**构建脚本**:
- `build_debug.bat` / `build_release.bat` - 命令行构建
- `install_apk.bat` - 智能APK安装管理工具
- Android Studio Build Variants面板集成

#### 记账簿功能完成（2025-08-20）
**完成了完整的记账簿管理系统，支持多账本独立记账**

**核心功能实现**:
1. **记账簿数据模型**
   - 新增 `LedgerEntity` 支持多账本结构
   - 数据库版本升级至v11 (添加记账簿支持)
   - 所有交易记录关联到指定记账簿

2. **记账簿管理界面**
   - 完整的CRUD操作（创建、编辑、删除、查看）
   - 记账簿列表显示和统计信息
   - 默认记账簿设置和排序功能
   - 美观的Material 3设计

3. **数据库修复和优化**
   - 修复Migration_11_12外键约束和索引问题
   - 解决应用启动崩溃问题
   - 确保数据完整性和性能优化

**关键问题修复**:
- ✅ 修复数据库迁移验证失败导致的应用崩溃
- ✅ 修复记账簿管理导航错误，添加缺失的composable定义  
- ✅ 修复LedgerApiImpl中getLedgerManagementScreen方法的NotImplementedError
- ✅ 完善外键约束确保数据引用完整性

**访问路径**: 
- 记账簿管理: 主界面 → 个人 → 记账设置 → 记账簿管理

#### 记账模块核心功能修复（2025-08-23）
**完成了交易记录编辑功能和关键技术债务清理，提升用户体验**

**核心功能实现**:
1. **交易编辑功能完善**
   - 修复了LedgerScreen中交易记录点击编辑功能
   - 实现了普通点击直接进入编辑模式的用户体验
   - 修复了AddTransactionScreen的保存线程安全问题
   - 完整支持交易数据的加载、编辑和保存流程

2. **用户界面优化**
   - 优化了长按菜单UI设计，统一使用Material 3设计规范
   - 将"编辑"菜单项改为"退款（待开发中）"，为未来功能预留
   - 添加了现代化的菜单分隔线、图标和品牌色彩

3. **关键技术债务清理** ⭐
   - **架构问题诊断**: 发现并解决了双重TransactionItem组件架构混淆
   - **僵尸代码清理**: 删除了从未使用的`components/TransactionItem.kt`文件
   - **组件调用链优化**: 确认使用`StyleableComponentFactory`→`StyleableTransactionItem`正确调用链

**技术架构发现**:
- 应用使用工厂模式支持不同UI风格（BALANCED/HIERARCHICAL）
- 旧组件是架构演进过程中的遗留代码，影响了开发效率
- 新架构通过StyleableComponentFactory提供统一的组件创建接口

**关键问题解决**:
- ✅ 修复交易记录点击无响应问题，实现完整编辑流程
- ✅ 解决线程安全问题，使用`Dispatchers.Main`确保UI操作安全
- ✅ 统一用户界面风格，提升产品一致性体验
- ✅ 清理技术债务，提升代码架构清晰度

#### 统一账户资产页面重构（2025-08-24） ⭐
**完成了资产总览与账户管理功能的统一整合，提供一站式账户资产管理体验**

**核心架构改进**:
1. **统一界面设计**
   - 创建`UnifiedAccountAssetScreen`采用Tab仪表板设计
   - "资产总览" + "账户管理"双Tab布局，解决功能分散问题
   - 支持HorizontalPager动画切换和响应式状态管理
   - 使用`@OptIn(ExperimentalFoundationApi::class)`正确处理实验性API

2. **内容组件重构**
   - **AssetOverviewContent**: 提取净资产、资产分布、趋势分析等完整功能
   - **AccountManagementContent**: 提取账户CRUD、分组显示、批量操作功能
   - 实现组件复用和模块化，原有功能100%保持

3. **导航架构优化**
   - 新增`UnifiedAccountAssetRoute`到导航系统和Screen定义
   - LedgerApi扩展`getUnifiedAccountAssetScreen`方法
   - 侧边栏LedgerDrawerContent从2个菜单项整合为1个"账户与资产"
   - 简化用户操作路径，提升导航效率

**技术实现亮点**:
- **ViewModel复用**: 两个Tab分别使用AssetOverviewViewModel和AccountViewModel
- **状态管理**: HorizontalPager与TabRow同步，支持程序化和用户手动切换
- **性能优化**: 避免数据重复加载，组件内容复用降低内存占用
- **代码复用**: 提取内容组件可在多个页面复用，提升开发效率

**解决的用户体验问题**:
- ❌ **原问题**: 用户需要在"资产总览"和"账户管理"间频繁跳转，功能割裂
- ✅ **新体验**: 单页面内Tab切换，一站式账户资产管理体验
- ✅ **效率提升**: 无需导航跳转，操作流程更加顺畅

**关键问题解决**:
- ✅ 整合分离的资产分析和账户管理功能为统一界面
- ✅ 通过组件重构实现代码复用，避免功能重复实现
- ✅ 优化侧边栏导航结构，简化菜单层级
- ✅ 完成编译验证，确保新架构稳定运行

**访问路径**: 
- 统一账户资产: 主界面 → 侧边栏 → 账户与资产

#### 自动记账导航链路修复与调试分析（2025-08-25） ⭐
**完成了自动记账权限设置和调试面板的导航链路修复，并建立了完整的调试分析体系**

**导航链路修复**:
1. **完整的4级导航修复**
   - ✅ Screen.kt: 添加缺失的`PermissionGuideRoute`路由定义
   - ✅ LedgerApi.kt: 扩展接口支持权限设置和调试面板导航参数
   - ✅ LedgerApiImpl.kt: 实现导航参数传递到具体页面组件
   - ✅ NavGraph.kt: 添加`PermissionGuideRoute`的composable定义和导航逻辑

2. **用户体验问题解决**
   - ❌ **原问题**: 点击"自动记账权限设置"和"自动记账调试面板"菜单无响应
   - ✅ **修复后**: 完整的导航链路，菜单点击正常跳转到对应页面
   - ✅ **架构完整性**: 确保模块化架构下的跨模块导航正确实现

**自动记账调试分析**:
3. **完整的调试架构评估**
   - 通知监听服务(PaymentNotificationListener): ✅ 架构完整，但缺少实时日志
   - 通知解析器(AlipayNotificationParser): ✅ 已包含"支出"关键词，逻辑完整
   - 自动记账管理器(AutoLedgerManager): ✅ 完整的业务流程，包含错误处理
   - 调试记录系统(RecordAutoLedgerDebugUseCase): ✅ 数据库记录完整

4. **6点调试检查方案设计**
   - 检查点1: 通知监听服务接收状态
   - 检查点2: 事件流发布机制
   - 检查点3: 通知解析处理过程
   - 检查点4: 去重验证逻辑
   - 检查点5: 账户分类推荐决策
   - 检查点6: 弹窗触发机制

**技术发现**:
- **支付宝解析器优化**: 确认已添加"支出"关键词到alipayExpenseKeywords
- **架构完整性**: 自动记账系统架构完整，具备完整的通知→解析→处理→弹窗流程
- **调试工具缺口**: 虽有数据库调试记录，但缺少实时logcat日志用于问题定位

**关键问题识别**:
- ✅ 导航链路问题：已完全修复，菜单功能正常
- ⚠️ 弹窗不显示问题：需要通过6点调试检查方案进一步定位具体失败环节
- ✅ 代码架构完整：自动记账功能的基础设施建设完善

**访问路径**:
- 自动记账权限设置: 主界面 → 个人 → 记账设置 → 自动记账权限设置
- 自动记账调试面板: 主界面 → 个人 → 记账设置 → 自动记账调试面板

#### 银行卡管理功能完成（2025-09-26） ⭐
**完成了完整的银行卡管理系统，支持多种卡片类型和详细信息管理**

**核心功能实现**:
1. **银行卡数据模型**
   - 新增 `CardEntity` 支持银行卡、信用卡、储蓄卡等多种类型
   - 数据库版本升级至v17 (添加cards表)
   - v18升级添加持卡人姓名和发卡机构字段
   - 支持卡片昵称、卡号、类型、持卡人等完整信息

2. **分类系统优化**
   - v20: 分类数据迁移和索引优化，支持更精确的分类查询
   - v21: 新增 `isHidden` 字段，自动隐藏转账相关分类
   - 转账分类自动标记为系统分类并隐藏，简化用户界面

**关键问题解决**:
- ✅ 建立完整的银行卡信息管理体系
- ✅ 优化分类索引，提升查询性能
- ✅ 自动化转账分类管理，提升用户体验

#### 统一通知系统完成（2025-10-04） ⭐
**完成了跨模块统一通知管理系统，实现任务、习惯、预算、信用卡等多场景通知**

**核心架构实现**:
1. **Notification共享模块**
   - 创建 `shared/notification` 模块作为通知基础设施
   - 提供统一的 `NotificationApi` 接口供各功能模块调用
   - 支持任务提醒、习惯打卡、预算超支、信用卡还款等多种通知类型
   - 集成WorkManager实现通知调度和定时任务

2. **通知管理功能**
   - `NotificationManager`: 核心通知发送和管理
   - `NotificationAccessController`: 通知权限控制
   - `NotificationEventRepository`: 通知事件追踪
   - `PaymentNotificationListener`: 支付通知监听（配合自动记账）

3. **UI界面整合**
   - 在个人页面添加"通知设置"入口
   - 提供分模块通知开关（任务、习惯、预算等）
   - 支持通知权限引导和状态检查

**技术架构亮点**:
- **模块解耦**: 通知功能独立为共享模块，避免功能模块间直接依赖
- **统一接口**: 各功能模块通过NotificationApi统一调用，易于维护
- **灵活配置**: 支持全局和单条记录的个性化提醒配置

**关键问题解决**:
- ✅ 解决各模块通知功能分散、重复实现的问题
- ✅ 建立统一的通知管理架构，提升代码复用性
- ✅ 提供友好的通知设置界面，增强用户控制能力

#### 增强提醒配置功能（2025-10-04） ⭐
**完成了任务和习惯的个性化提醒配置系统，支持单条记录自定义提醒**

**核心功能实现**:
1. **任务提醒增强** (v22-v23)
   - 新增 `reminderEnabled`、`reminderAt`、`reminderMinutesBefore` 字段
   - v23新增 `reminderTime` 支持固定时间提醒（如每天9:00）
   - 支持三种提醒模式：
     - 截止时间前N分钟提醒
     - 指定时间点提醒
     - 固定时间提醒（每日重复）

2. **习惯提醒增强** (v22)
   - 新增 `reminderEnabled`、`reminderTime` 字段
   - 支持单条习惯自定义提醒时间
   - 可选继承全局配置或独立配置

3. **配置继承机制**
   - 字段默认为null，表示使用全局配置
   - 设置具体值后使用单条记录配置
   - 灵活平衡便捷性和个性化需求

**用户体验提升**:
- ✅ 支持重要任务单独设置提醒，避免全局配置限制
- ✅ 习惯可根据个人作息时间灵活调整提醒
- ✅ 继承机制降低配置复杂度，默认沿用全局设置

#### 桌面小部件开发完成（2025-10-04） ⭐
**完成了记账小部件和倒计时小部件，支持桌面快捷访问和实时数据展示**

**核心功能实现**:
1. **记账小部件** (`LedgerWidgetProvider`)
   - 实时显示今日收支统计和账户余额
   - 支持快捷记账按钮，一键打开记账界面
   - 自动定时更新数据（通过WorkManager）
   - 提供配置界面 (`LedgerWidgetConfigActivity`) 自定义显示内容
   - 支持多实例，可添加多个小部件显示不同账本

2. **倒计时小部件** (`CountdownWidgetProvider`)
   - 显示重要日期倒计时
   - 支持自定义倒计时事件
   - 自动更新剩余天数

3. **技术实现**
   - `WidgetUpdateScheduler`: 统一的小部件更新调度
   - `LedgerWidgetWorker`: 后台数据更新Worker
   - `LedgerWidgetCleanupWorker`: 小部件清理Worker
   - `WidgetRefreshBroadcaster`: 数据变更时触发小部件刷新
   - `LedgerWidgetPreferences`: 小部件配置持久化

**用户体验提升**:
- ✅ 无需打开应用即可查看关键财务信息
- ✅ 桌面快捷记账，提升记账效率
- ✅ 实时数据同步，确保信息准确性

#### Demo布局优化和组件开发（2025-09-26 ~ 2025-09-30）
**完成了Demo环境的系统性优化，建立了可复用组件体系**

**核心改进**:
1. **Demo布局重构**
   - 优化Demo页面结构和导航逻辑
   - 统一组件展示风格
   - 改进代码组织和可维护性

2. **日期选择器组件** (2025-09-30)
   - 完成 `CustomDatePickerDialog` 可复用组件
   - 已记录到 `doc/demo开发/可复用组件清单.md`
   - 提供完整的参数配置和使用示例

3. **组件文档化流程**
   - 建立组件清单文档维护规范
   - 所有Demo可复用组件必须记录到组件清单
   - 包含函数签名、参数说明、使用示例等完整信息

**开发规范建立**:
- ✅ Demo组件开发标准流程
- ✅ 组件文档化强制要求
- ✅ 提升组件复用率和团队协作效率

#### 记一笔V2布局调整（2025-10-05）
**正式页面回退到Demo环境，保持迭代灵活性**

**调整决策**:
- ❌ 移除正式环境的AddTransactionV2Screen.kt
- ✅ 保留Demo环境的AddBillScreenV2.kt继续演进
- ✅ 新增AssetPageScreen.kt资产页面Demo

**技术原因**:
- Demo环境允许更激进的UI实验，不影响生产稳定性
- V2布局需要更多真实用户测试和反馈
- 保持V1作为稳定基线，V2作为创新实验室

**访问路径**:
- Demo环境: StyleCatalogDemoActivity（仅Debug版本）
- 生产页面: AddTransactionScreen.kt（V1稳定版）

#### 依赖管理现代化（2025-10-05）
**完成Version Catalog命名统一和插件alias迁移**

**核心改进**:
1. **统一依赖命名规范**
   - Kotlin系: 统一使用`kotlinx-*`前缀
   - Compose系: 统一使用`androidx-compose-*`前缀
   - 移除冗余的双重命名（kotlin-coroutines vs kotlinx-coroutines-android）

2. **插件配置现代化**
   - 全模块迁移到`alias(libs.plugins.*)`语法
   - 移除硬编码的插件ID和版本号
   - 提升构建脚本可维护性

3. **影响范围**
   - ✅ app模块: 已迁移
   - ✅ shared/notification模块: 已迁移
   - ✅ 其他模块: 陆续完成

**技术债务改善**:
- 降低版本管理复杂度
- 避免依赖版本冲突
- 符合Gradle最佳实践

## File Organization Standards

### Document and Script Organization
```
项目根目录/
├── doc/                    # All project documentation
│   ├── 架构迁移计划与原则.md
│   ├── 架构迁移进度追踪.md  
│   └── [module]迁移总结.md
├── scripts/                # Auxiliary scripts
│   └── *.sh/*.bat         
├── app/                   
├── core/                  
├── feature/               
└── shared/                
```

**Rules**:
- All .md files must be in `doc/` folder
- All scripts must be in `scripts/` folder
- Keep Android standard structure for everything else

## Known Issues and Solutions

### Common Issues
1. **Java 21 + AGP 8.1.x Compatibility**
   - Solution: Upgrade AGP to 8.2.1 or higher

2. **Hilt Duplicate Bindings**
   - Solution: Remove duplicate providers

3. **Data Module Naming Inconsistency**
   - Issue: Schedule module uses snake_case
   - Solution: Handle conversion in mappers

### ✅ Recently Resolved Issues
4. **Database Migration Validation Failure (2025-08-20)**
   - Issue: Room migration validation failed due to missing foreign keys and incorrect index names
   - Solution: Created Migration_11_12 to properly recreate transactions table with foreign key constraints
   - Status: Fixed in database version 12

5. **NotImplementedError in LedgerApiImpl (2025-08-20)**
   - Issue: getLedgerManagementScreen method threw NotImplementedError
   - Solution: Updated method to call existing LedgerBookManagementScreen component
   - Status: Fixed and functional

6. **Auto Ledger Navigation Chain Broken (2025-08-25)**
   - Issue: Clicking "自动记账权限设置" and "自动记账调试面板" menu items had no response
   - Root Cause: Navigation chain broken at 4 levels - missing route definition, API parameters, implementation, and NavGraph composables
   - Solution: Fixed complete navigation chain
     - Added `PermissionGuideRoute` to Screen.kt
     - Extended LedgerApi with navigation parameters
     - Implemented parameter passing in LedgerApiImpl
     - Added composable definition in NavGraph.kt
   - Status: Fixed and functional

### 🔍 Current Investigation Issues
7. **Auto Ledger Popup Not Appearing (2025-08-25)**
   - Issue: Alipay payment notifications (0.01元 支出) not triggering auto ledger popup despite all permissions enabled
   - Analysis: Complete debugging architecture established with 6-point diagnostic approach
   - Status: Investigation in progress, debugging system designed
   - Next Steps: Implement detailed logging at key checkpoints to identify failure point

## Important Reminders
1. **Database Version**: Currently at version 23 (enhanced notification and reminder system)
2. **Navigation**: Only 2 items active (Home, Profile)
3. **Technical Debt**: Ongoing cleanup, ~82% healthy
4. **Import/Export**: Fully functional with recent fixes
5. **MCP Server**: Configured and available for compilation
6. **APK Signing**: Release版本使用自定义签名 (ccxiaoji_release.keystore)
7. **Version Management**: Debug和Release版本可并存，支持快速切换
8. **Ledger Notebook System**: Complete multi-ledger support with CRUD operations
9. **Unified Account Asset Management**: Tab-based dashboard providing integrated asset and account management experience
10. **Transfer System**: Complete transfer functionality with CreateTransferUseCase and linked transaction records
11. **Bank Card Management**: Complete card management with CardEntity and detailed information
12. **Notification System**: Unified notification module (shared/notification) for cross-module notification management
13. **Desktop Widgets**: Ledger and countdown widgets with real-time data sync
14. **Enhanced Reminders**: Task and habit individual reminder configuration support
15. **Demo Component Documentation**: ⭐ All demo components must be documented in `doc/demo开发/可复用组件清单.md`

## Related Documentation
- `doc/架构迁移计划与原则.md` - Architecture migration principles
- `doc/20250813-记账数据导出功能实施.md` - Export implementation
- `doc/20250815-数据导入问题修复.md` - Import fixes
- `doc/20250824-统一账户资产页面重构完整实施.md` - Unified account asset page refactoring
- `doc/20250627-技术债务真实状态评估报告.md` - Technical debt assessment
- `doc/demo开发/可复用组件清单.md` - ⭐ Demo reusable component documentation
- `Debug与Release版本切换指南.md` - Debug/Release版本切换完整指南
- `版本切换快速参考.md` - 版本切换快速参考卡片
- `Android Studio一键构建APK设置指南.md` - Android Studio签名配置指南
- `一键构建APK快速指南.md` - APK构建快速参考

---
*Last Updated: 2025-10-05 - 技术栈升级（Kotlin 1.9.24, Compose BOM 2024.10.00, Hilt 2.51.1）；记一笔V2回退到Demo环境；完成依赖管理现代化（Version Catalog统一命名）*