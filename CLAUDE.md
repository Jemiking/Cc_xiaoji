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
- **Room database version**: 16 (Current - Transfer functionality and advanced ledger features)
- **Tables**: 24 tables total
- **Distribution**: 
  - Ledger: 10 tables (including Ledger notebooks)
  - Schedule: 4 tables
  - Plan: 3 tables
  - Habit: 2 tables
  - Todo: 1 table
  - Others: 4 tables
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
- **Language**: Kotlin 1.9.21
- **Build System**: Gradle 8.9 + Android Gradle Plugin 8.2.1
- **Compose BOM**: 2024.02.00
- **Compose Compiler**: 1.5.7

### Key Libraries
- **Dependency Injection**: Hilt 2.48.1
- **Database**: Room 2.6.1
- **Networking**: Retrofit 2.9.0, OkHttp 4.12.0
- **Async**: Coroutines 1.7.3, Flow
- **Background**: WorkManager 2.9.0
- **Navigation**: Navigation Compose 2.7.6
- **DateTime**: Kotlinx DateTime 0.5.0

## Current Project Status (2025-08-25)

### ✅ Completed Features
1. **Architecture Migration**: 100% complete with 13 modules
2. **Navigation**: 2 active bottom navigation items (Home, Profile)
   - Other modules (Ledger, Todo, Habit, Schedule) are commented but ready
3. **Database**: Version 16 with transfer functionality and advanced ledger features
4. **Core Features**: Todo, Habit, Ledger all functional
5. **Schedule Module**: Successfully integrated
6. **Plan Module**: Successfully integrated with tree structure
7. **Ledger Notebook System**: Complete ledger book management functionality
8. **Unified Account Asset Management**: Tab-based dashboard integrating asset overview and account management
9. **Transfer Functionality**: Complete transfer transaction system with linked records
10. **Auto Ledger System**: Complete notification-based automatic bookkeeping infrastructure
11. **Test Framework**: JUnit + MockK + Truth configured

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
1. **Database Version**: Currently at version 16 (transfer functionality and advanced ledger features)
2. **Navigation**: Only 2 items active (Home, Profile)
3. **Technical Debt**: Ongoing cleanup, ~82% healthy
4. **Import/Export**: Fully functional with recent fixes
5. **MCP Server**: Configured and available for compilation
6. **APK Signing**: Release版本使用自定义签名 (ccxiaoji_release.keystore)
7. **Version Management**: Debug和Release版本可并存，支持快速切换
8. **Ledger Notebook System**: Complete multi-ledger support with CRUD operations
9. **Unified Account Asset Management**: Tab-based dashboard providing integrated asset and account management experience
10. **Transfer System**: Complete transfer functionality with CreateTransferUseCase and linked transaction records

## Related Documentation
- `doc/架构迁移计划与原则.md` - Architecture migration principles
- `doc/20250813-记账数据导出功能实施.md` - Export implementation
- `doc/20250815-数据导入问题修复.md` - Import fixes
- `doc/20250824-统一账户资产页面重构完整实施.md` - Unified account asset page refactoring
- `doc/20250627-技术债务真实状态评估报告.md` - Technical debt assessment
- `Debug与Release版本切换指南.md` - Debug/Release版本切换完整指南
- `版本切换快速参考.md` - 版本切换快速参考卡片
- `Android Studio一键构建APK设置指南.md` - Android Studio签名配置指南
- `一键构建APK快速指南.md` - APK构建快速参考

---
*Last Updated: 2025-08-25 - 完成自动记账导航链路修复与调试分析；修复4级导航链路断裂问题，建立6点调试检查方案，为自动记账功能问题定位奠定基础*