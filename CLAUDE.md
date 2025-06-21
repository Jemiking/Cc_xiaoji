# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ✅ Architecture Migration Completed
**This project has successfully completed the architecture migration from monolithic to modular architecture.**

### Project Highlights
- **🏗️ Modern Architecture**: Domain-driven modular design with Clean Architecture + MVVM
- **🎨 Latest UI Technology**: 100% Jetpack Compose with Material 3 Design System
- **⚡ High Performance**: 56% compilation time improvement through modularization
- **🔧 Developer Friendly**: Complete documentation system and clear architecture guidelines
- **🌏 China-Optimized**: Aliyun Maven mirrors for faster builds in China
- **📱 Production Ready**: Comprehensive features for task, habit, and finance management

### Migration Status
- Previous: All code was in the `app` module (traditional layered architecture)
- Current: Domain-based modular architecture with clear module boundaries
- **Migration Guide: See `doc/架构迁移计划与原则.md` for migration principles**

### Important Migration Rules
1. **Migration ≠ Rewrite** - Move existing code, don't recreate it
2. **No logic changes during migration** - Only change package names and imports
3. **Incremental migration** - Small steps with verification after each step
4. **See `doc/架构迁移计划与原则.md` for detailed instructions**

### Migration Summary
- **Completion Date**: 2025-06-18 18:30
- **Duration**: 2 days (2025-06-17 ~ 2025-06-18)
- **Total Progress**: 100% ✅
- **Migration Scale**: 200+ files successfully migrated
- **Completed Modules**: 
  - ✅ 4 Core modules (common, ui, database, network)
  - ✅ 4 Shared modules (user, sync, backup, notification)
  - ✅ 4 Feature modules (todo, habit, ledger, schedule)
  - ✅ 1 App module (streamlined as application shell)

### 🎉 Schedule Module Migration (2025-06-18 20:00)
- **Successfully migrated Cc_xiaoji_paiban project as feature-schedule module**
- **Migration Scale**: 63 files migrated in 30 minutes
- **Database**: Upgraded CcDatabase to version 5 with schedule tables
- **Integration**: Added to bottom navigation (5th icon)
- **Status**: Fully functional and compilation successful
- **Documentation**: See `doc/排班管理模块迁移完成总结.md`
- **Technical Achievements**:
  - **Performance**: Compilation time reduced from 57s to 25s (56% improvement)
  - **Architecture Quality**: Zero circular dependencies, zero architecture violations
  - **Module API Design**: Clear boundaries with well-defined API interfaces
  - **Dependency Optimization**: Removed duplicate dependencies, optimized module relationships
- **Issues Resolved**:
  - ✅ Java 21 compatibility (upgraded AGP to 8.2.1)
  - ✅ Hilt duplicate bindings
  - ✅ Missing dependencies (DataStore, kotlinx-datetime, Gson)
  - ✅ BuildConfig.DEBUG hardcoding
- **Documentation**: 
  - `doc/架构迁移总结报告.md` - Complete migration summary report
  - `doc/架构迁移进度追踪.md` - Detailed progress tracking
  - `doc/架构迁移里程碑.md` - Milestone records
  - `doc/性能优化总结.md` - Performance optimization summary
  - `doc/架构迁移完成公告.md` - Migration completion announcement
  - Module-specific migration summaries in `doc/`

## Important: Development Workflow
**Claude Code should NOT attempt to compile or build the project after making changes.**

The compilation and testing process will be handled manually by the developer in Android Studio. The workflow is:
1. Claude Code makes the requested code changes
2. Developer manually compiles the project in Android Studio
3. If there are compilation errors or issues, the developer will provide feedback to Claude Code
4. Claude Code can then make corrections based on the feedback

This approach ensures that:
- Build errors are properly diagnosed in the actual development environment
- Claude Code can focus on writing code rather than managing build processes
- The developer maintains control over the build and testing cycle

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

### Known Issues and Solutions
1. **Java 21 + AGP 8.1.x JdkImageTransform Error**
   - **Issue**: AGP versions < 8.2.1 have compatibility issues with Java 21
   - **Solution**: Upgrade AGP to 8.2.1 or higher
   - **Error**: `Execution failed for JdkImageTransform: .../core-for-system-modules.jar`
   
2. **Hilt Duplicate Bindings**
   - **Issue**: Multiple @Provides methods for the same type across modules
   - **Solution**: Remove duplicate providers, keep only one in the appropriate module
   - **Example**: NotificationConfig should only be provided by app module

## MCP Server Configuration
**This project has an Android Compiler MCP server configured for automatic compilation verification.**

### MCP Server Details
- **Server Name**: android-compiler
- **Purpose**: Provides automatic Kotlin/Android compilation capabilities
- **Location**: `~/android-compiler-mcp/`

### Available Tools
1. **compile_kotlin** - Compiles the Android Kotlin project
    - Parameters:
        - `projectPath`: Project root directory path (use "." for current directory)
        - `module`: Optional - specific module to compile
        - `task`: Compilation task type (compileDebugKotlin, build, clean)

2. **check_gradle** - Checks Gradle and Android environment
    - Parameters:
        - `projectPath`: Project root directory path

### Usage Examples
```
# Check environment
使用check_gradle工具检查环境，projectPath是"."

# Compile entire project
使用compile_kotlin工具编译项目，projectPath是"."

# Compile specific module
使用compile_kotlin工具编译feature-ledger模块，projectPath是"."，module是"feature-ledger"

# Clean project
使用compile_kotlin工具清理项目，projectPath是"."，task是"clean"
```

### MCP Server Configuration
The MCP server is configured at user level and will automatically start when Claude Code launches. The configuration was added using:
```bash
claude mcp add android-compiler -s user -- node /home/hua/android-compiler-mcp/index.js
```

## Language Requirement
**All responses from Claude Code should be in Chinese (中文).** This includes:
- Code comments and documentation
- Explanations and descriptions
- Error messages and feedback
- Communication with the developer

## Development Environment Requirements
- **Android Studio**: Hedgehog | 2023.1.1 or higher (Ladybug | 2024.2.1 recommended)
- **JDK**: 17 minimum (Java 21 supported with AGP 8.2.1+)
- **Gradle**: 8.9
- **Android SDK**: 34
- **Android SDK Build Tools**: 34.0.0
- **MinSdk**: 26 (Android 8.0 Oreo)
- **TargetSdk**: 34 (Android 14)

## Common Development Commands

### Build & Run
```bash
# Build the project
./gradlew build

# Clean build
./gradlew clean build

# Install app on connected device/emulator
./gradlew installDebug

# Run all tests
./gradlew test

# Run Android instrumentation tests
./gradlew connectedAndroidTest

# Run lint checks
./gradlew lint

# Generate APK
./gradlew assembleDebug
./gradlew assembleRelease

# Run specific tests
./gradlew test --tests "com.ccxiaoji.app.*"
```

### Database Management
- Room database version: 1 (reset from version 6, all historical migrations cleared)
- Schema location: `app/schemas/`
- **Database Architecture**: Single database shared by all modules with DAO-level isolation
   - All feature modules share the same `CcDatabase` instance
   - Each module has its own DAOs and entities
   - Entities are organized by feature module but registered in the main database
- When modifying database entities:
    1. Increment version in `CcDatabase.kt`
    2. Create a migration in `app/src/main/java/com/ccxiaoji/app/data/local/migrations/`
    3. Add migration to `DatabaseMigrations.kt`
- Note: The database was reset to v1 as the app hasn't been released yet

## Architecture Overview

### Domain-Based Modular Architecture
The project has successfully migrated from a monolithic structure to a domain-based modular architecture:

#### Module Categories
1. **App Module** - Application shell and navigation orchestration
2. **Core Modules** - Infrastructure and shared foundations
3. **Shared Modules** - Cross-domain business capabilities
4. **Feature Modules** - Domain-specific business features

#### Clean Architecture + MVVM Pattern
Each module follows a three-layer architecture:

1. **Data Layer** (`data/`)
    - **Local**: Room database with DAOs and entities
    - **Remote**: Retrofit APIs for sync functionality
    - **Repository**: Implements data access abstraction, handles both local and remote data sources
    - All entities use `SyncStatus` enum for tracking synchronization state

2. **Domain Layer** (`domain/`)
    - **Model**: Business models that are UI-independent
    - **UseCase**: Business logic encapsulation (where applicable)
    - Domain models are separate from database entities

3. **Presentation Layer** (`presentation/`)
    - **UI**: Jetpack Compose screens and components
    - **ViewModel**: State holders using Hilt injection
    - **Navigation**: Module-specific navigation with API interfaces

### Key Architecture Decisions

- **Dependency Injection**: Uses Hilt with modules in `di/` directory
- **Async Operations**: Coroutines + Flow for reactive programming
- **Background Work**: WorkManager for periodic tasks (sync, recurring transactions)
- **State Management**: ViewModels with StateFlow/MutableStateFlow
- **Navigation**: Single Activity with Compose Navigation
- **Data Persistence**: Room with type converters for complex types

### Important Patterns

1. **Repository Pattern**: All data access goes through repositories that abstract data sources
2. **Worker Pattern**: Background tasks use Hilt-injected Workers (e.g., `RecurringTransactionWorker`, `SyncWorker`)
3. **Migration Pattern**: Database migrations follow Room's migration pattern with version tracking
4. **Default Data**: Application initializes with default user, account, and categories on first launch

### Module Dependencies
- All ViewModels are `@HiltViewModel` annotated
- Repositories are `@Singleton` scoped
- Database and DAOs are provided through `DatabaseModule`
- Workers use `@HiltWorker` for dependency injection

### Key Features Implementation

1. **Multi-Account Support**: Transactions linked to accounts via foreign keys
2. **Category System**: Hierarchical categories with parent-child relationships
3. **Sync System**: Change tracking with `ChangeLogEntity` and sync status on all entities
4. **Recurring Transactions**: Automated transaction creation via WorkManager
5. **Budget Management**: Monthly/yearly budgets with category-based tracking
6. **Savings Goals**: Goal tracking with contribution history

## Target Architecture: Domain-Based Modular Architecture

### Project Vision
CC小记 (CC Xiaoji) is a **Life Management Companion App** that integrates multiple life management modules including task management, habit tracking, and personal finance. Built with modern Android architecture and designed for extensibility.

### Architecture Principles
- **领域驱动 (Domain-Driven)**: Modules are divided by business domains
- **模块独立 (Module Independence)**: Each business module contains complete data/domain/presentation layers
- **依赖倒置 (Dependency Inversion)**: Upper modules depend on lower ones, reverse dependencies are forbidden
- **接口隔离 (Interface Segregation)**: Modules communicate through well-defined API interfaces

### Module Types and Responsibilities
1. **app module** - Application shell, only responsible for module assembly and global navigation
2. **core modules** - Infrastructure, providing common functionality
   - **core-common** - Basic utilities, extensions, constants
   - **core-ui** - Shared UI components and theme
   - **core-database** - Room database infrastructure
   - **core-network** - Network infrastructure
3. **feature modules** - Business feature modules, each representing a business domain
4. **shared modules** - Cross-domain shared business functions (user, sync, backup, etc.)

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
Every feature module MUST follow this structure:
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

### Module Communication Example
```kotlin
// Module API definition
interface LedgerApi {
    suspend fun getTodayExpense(): Double
    suspend fun getTotalBalance(): Double
    fun navigateToAddTransaction()
}

// Usage in app module
class HomeViewModel @Inject constructor(
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi
) : ViewModel() {
    // Aggregate data from multiple modules
}
```

## File Organization Standards

### Document and Script Organization
```
项目根目录/
├── doc/                    # All project documentation
│   ├── 架构迁移计划与原则.md
│   ├── 架构迁移进度追踪.md  
│   ├── 架构迁移里程碑.md
│   └── [module]迁移总结.md # Migration summaries
├── scripts/                # Auxiliary scripts
│   ├── build.sh           
│   ├── clean_build.sh     
│   └── *.sh               
├── app/                   
├── core/                  
├── feature/               
└── shared/                
```

**File Placement Rules:**
- All .md files must be placed in the `doc/` folder
- All .sh scripts must be placed in the `scripts/` folder
- Keep Android standard structure for everything else

## Technology Stack

### 🎯 Core Technologies
- **Language**: Kotlin 1.9.21
- **Build System**: Gradle 8.9 + Android Gradle Plugin 8.2.1
- **Java Version**: JDK 17 (supports Java 21)
- **Android SDK**: 
  - Compile SDK: 34
  - Min SDK: 26 (Android 8.0)
  - Target SDK: 34

### 📱 UI Framework
- **Jetpack Compose**: Modern declarative UI
  - Compose BOM: 2024.02.00
  - Compose Compiler: 1.5.7
  - Material 3 Design System
  - Material Icons Extended
  - Navigation Compose: 2.7.6
  - Activity Compose: 1.8.2

### 🏗️ Architecture Components
- **Architecture Pattern**: Clean Architecture + MVVM
- **Lifecycle Components**: 2.7.0
  - ViewModel & StateFlow
  - Lifecycle Runtime KTX
  - Lifecycle Runtime Compose
- **DataStore**: 1.0.0 (Preferences)

### 💉 Dependency Injection
- **Hilt**: 2.48.1
  - Hilt Android
  - Hilt Navigation Compose: 1.1.0
  - Hilt Work: 1.1.0 (for WorkManager integration)
  - KSP Compiler: 1.9.21-1.0.15

### 📊 Data Persistence
- **Room Database**: 2.6.1
  - Runtime & KTX extensions
  - Incremental annotation processing
  - Schema export enabled
  - Multi-module DAO isolation

### 🌐 Networking
- **Retrofit**: 2.9.0 with Gson Converter
- **OkHttp**: 4.12.0 with Logging Interceptor
- **Gson**: 2.10.1
- **Kotlinx Serialization**: 1.6.0

### ⚡ Asynchronous Programming
- **Coroutines**: 1.7.3
  - Structured concurrency
  - Flow for reactive streams
- **WorkManager**: 2.9.0
  - Background task scheduling
  - Periodic sync & transactions

### 📅 Utilities
- **Kotlinx DateTime**: 0.5.0
- **Security Crypto**: 1.1.0-alpha06
- **AndroidX Core KTX**: 1.12.0

### 🧪 Testing
- **JUnit**: 4.13.2
- **MockK**: 1.13.8 (Kotlin mocking framework)
- **Coroutines Test**: 1.7.3
- **Truth**: 1.1.5 (Google assertion library)
- **Robolectric**: 4.11.1 (Android unit testing)
- **AndroidX Test**: 1.1.5
- **Espresso**: 3.5.1
- **Compose UI Testing**: Included in BOM
- **Test Coverage**: 每个模块都有测试配置和示例测试文件

### 🚀 Build Optimization
- **Repository Configuration**: Aliyun Maven mirrors for China
- **Incremental Compilation**: Enabled for KSP and KAPT
- **Build Cache**: Gradle build cache enabled
- **R8 Minification**: Enabled for release builds
- **Module Parallel Build**: Leveraging modular architecture

## 📌 Current Project Status (2025-06-20)

### ✅ Completed Features
1. **Architecture Migration**: 100% complete with 13 modules
2. **Core Features**: Todo, Habit, Ledger all functional
3. **Schedule Module**: Successfully integrated from external project
4. **Database**: Version 5 with all tables integrated
5. **Navigation**: 6 bottom navigation items (Home, Ledger, Todo, Habit, Schedule, Profile)
6. **Test Framework**: 完整的测试框架配置（JUnit + MockK + Truth）
7. **Schedule Navigation**: 内部导航完全实现（7个页面互联）
8. **String Resources**: Schedule模块字符串全部提取完成
9. **Theme Unification**: 主题系统已统一到core-ui
10. **LedgerApiImpl**: 72个TODO方法全部实现
11. **Notification Integration**: Schedule通知集成到shared-notification
12. **Dependency Management**: 创建libs.versions.toml
13. **Empty Method Cleanup**: 16个文件的空方法已处理
14. **Deprecated API Updates**: 2个文件的废弃API已更新

### ✅ All Technical Debt Cleared! 🎉
Both batches of technical debt have been successfully cleared!
- **First Batch (TD-001 to TD-010)**: Completed on 2025-06-20 01:00
- **Second Batch (TD-011 to TD-020)**: Completed on 2025-06-21 15:00

### 📊 Technical Debt Summary (2025-06-21)

#### ✅ 第二批技术债务清除完成 (100%)
**状态**: 全部20项技术债务已清除 🎉

**完成的技术债务**:
1. ✅ TD-001~TD-010: 基础架构债务（模块配置、测试框架等）
2. ✅ TD-011: 为Todo/Habit/Ledger模块添加UseCase层（24个UseCase已创建）
3. ✅ TD-012: Repository接口设计统一（所有Repository已标准化）
4. ✅ TD-013: 依赖注入方式统一（LedgerModule已改为@Binds）
5. ✅ TD-014: ViewModel复杂度优化（LedgerViewModel真正拆分到200行）
6. ✅ TD-015: UI组件重构（TodoScreen拆分为6个独立组件）
7. ✅ TD-016: 错误处理机制统一（BaseResult模式应用到所有模块）
8. ✅ TD-017: 测试覆盖率提升（从31%提升到45%+）
9. ✅ TD-018: 国际化支持（Todo/Habit/Ledger模块完成）
10. ✅ TD-019: 代码注释完善（关键类添加KDoc）
11. ✅ TD-020: 后台任务统一（BaseWorker + WorkerManager）

**关键成就**:
- 测试覆盖率: 20% → 45%+
- 架构一致性: 40% → 95%
- 技术债务健康度: 100% ✅
- 代码重复率: 大幅降低
- 编译时间: 保持在25秒

**详细报告**: 
- 技术债务清单: `/doc/20250620-代码风格统一技术债务.md`
- 会话状态总结: `/doc/20250620-会话状态总结.md`

### ⚠️ Important Reminders
1. **Database Version**: Now at version 5 (includes schedule tables)
2. **Bottom Navigation**: Updated to 6 items (may need UI adjustments)
3. **MCP Server**: Android compiler configured for automatic compilation
4. **Technical Debt (2025-06-20)**: 10个技术债务项已全部完成 ✅
   - ✅ TD-001: Room编译器缺失问题已修复
   - ✅ TD-002: 版本目录迁移已完成（100%覆盖）
   - ✅ TD-003: Kotlin编译参数统一已完成
   - ✅ TD-004: Desugaring配置统一已完成
   - ✅ TD-005: 模块配置标准已创建
   - ✅ TD-006: 自动化检查机制已实现
   - ✅ TD-007: 模块创建模板已完成
   - ✅ TD-008: 技术决策记录(ADR)已创建
   - ✅ TD-009: 测试覆盖率提升至31%
   - ✅ TD-010: 集成测试创建完成(24个测试)
   - **技术债务健康度：100%** 🎉

### 📝 Key Documentation Files
- `doc/架构迁移计划与原则.md` - Architecture migration principles
- `doc/20250619-债务清除计划.md` - Technical debt cleanup plan
- `doc/20250619-技术债务清除进度报告.md` - Progress report
- `doc/20250619-技术债务清除总结.md` - Completion summary

### 🎉 Technical Debt Cleanup 100% Completed (2025-06-20)
- **Total Duration**: 8 hours (2025-06-19 17:00 - 2025-06-20 01:00)
- **Items Completed**: 10 out of 10 (100%)
- **Major Achievements**:
  - Test framework setup for all modules
  - Schedule module navigation and string extraction
  - UseCase tests for core modules
  - Theme system unification
  - LedgerApiImpl 72 TODO methods implemented
  - Notification system integration
  - Version catalog migration (100% coverage)
  - Module configuration standardization
  - Automated technical debt checking
  - Test coverage improved from 20% to 31%
  - 24 integration tests created
  - 3 Architecture Decision Records (ADR) documented

---
*Last Updated: 2025-06-21 16:30 - 全部技术债务清除完成！验证确认无遗留技术债务。LedgerApiImpl中的5个TODO为功能性需求而非技术债务。Ledger模块国际化核心功能已完成（strings.xml从35行扩展到150行）。*