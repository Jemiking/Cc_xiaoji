# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Current Migration Status (Updated: 2025-01-09)
**架构迁移进度：正在进行步骤 3.3 - Ledger模块迁移**

### 已完成的迁移
- ✅ 基础模块（core-common, core-ui, core-database）
- ✅ Todo模块（feature-todo）
- ✅ Habit模块（feature-habit）
- ✅ Ledger模块-统计功能
- ✅ Ledger模块-分类管理
- ✅ Ledger模块-交易记录
- ⏳ Ledger模块-账户管理（待进行）

### 下次继续
1. 等待用户确认交易记录功能编译结果
2. 继续迁移Ledger模块的账户管理功能
3. 详细进度见：`doc/架构迁移进度追踪.md`

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
**Before implementing any bug fix or solution, Claude Code must:**

1. **Present multiple solution options** (typically 2-3 different approaches)
2. **Analyze pros and cons for each solution:**
   - **优点 (Pros)**: Performance impact, maintainability, code simplicity, compatibility
   - **缺点 (Cons)**: Implementation complexity, potential risks, technical debt, limitations
3. **Provide a clear recommendation with reasoning:**
   - Explicitly state which solution is recommended
   - Explain why this solution is best for the specific context
   - Consider project architecture, existing patterns, and long-term maintainability

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

方案二：[方案名称]
- 优点：
  • [优点1]
  • [优点2]
- 缺点：
  • [缺点1]
  • [缺点2]

推荐方案：方案X
理由：[详细解释为什么推荐这个方案]
```

This ensures informed decision-making and helps developers understand the trade-offs of different approaches.

## Language Requirement
**All responses from Claude Code should be in Chinese (中文).** This includes:
- Code comments and documentation
- Explanations and descriptions
- Error messages and feedback
- Communication with the developer

## Development Environment Requirements
- Android Studio Hedgehog | 2023.1.1 or higher
- JDK 17
- Gradle 8.4
- Android SDK 34
- MinSdk: 26 (Android 8.0)

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
   1. Place entities in the appropriate feature module's `data/local/entity/` directory
   2. Register entities in `CcDatabase.kt`
   3. Increment version in `CcDatabase.kt`
   4. Create a migration in `app/src/main/java/com/ccxiaoji/app/data/local/migrations/`
   5. Add migration to `DatabaseMigrations.kt`
- Note: The database was reset to v1 as the app hasn't been released yet

## Architecture Overview

### Clean Architecture + MVVM Pattern
The project follows a three-layer architecture:

1. **Data Layer** (`data/`)
   - **Local**: Room database with DAOs and entities
   - **Remote**: Retrofit APIs for sync functionality
   - **Repository**: Implements data access abstraction, handles both local and remote data sources
   - All entities use `SyncStatus` enum for tracking synchronization state

2. **Domain Layer** (`domain/`)
   - **Model**: Business models that are UI-independent
   - Domain models are separate from database entities

3. **Presentation Layer** (`presentation/`)
   - **UI**: Jetpack Compose screens and components
   - **ViewModel**: State holders using Hilt injection
   - Navigation managed by `NavGraph.kt` with defined routes

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

## Target Architecture: Domain-Based Modular Architecture

### Project Vision
CC小记 (CC Xiaoji) is positioned as a **Life Management Super App** that integrates multiple life management modules and will continuously add new feature modules in the future.

### Architecture Principles
- **领域驱动 (Domain-Driven)**: Modules are divided by life management domains, not technical functions
- **模块独立 (Module Independence)**: Each business module contains complete data/domain/presentation layers
- **依赖倒置 (Dependency Inversion)**: Upper modules depend on lower ones, reverse dependencies are forbidden
- **接口隔离 (Interface Segregation)**: Modules communicate through well-defined API interfaces

### Module Types and Responsibilities
1. **app module** - Application shell, only responsible for module assembly and global navigation
2. **core modules** - Infrastructure, providing common functionality
3. **feature modules** - Business feature modules, each representing a life domain
4. **shared modules** - Cross-domain shared business functions

### Dependency Rules
```
✅ Allowed:
app → feature → shared → core

❌ Forbidden:
feature → feature
core → feature
core → shared
```

### Recommended Directory Structure
```
Cc_xiaoji/
├── app/                              # Main application module (shell)
│   ├── src/main/
│   │   ├── CcXiaoJiApplication.kt
│   │   ├── MainActivity.kt
│   │   ├── navigation/              # Global navigation
│   │   │   ├── AppNavGraph.kt      
│   │   │   └── BottomNavigation.kt 
│   │   └── home/                    # Home aggregation
│   │       └── HomeScreen.kt        
│
├── core/                            # Core infrastructure
│   ├── common/                      # Common functionality
│   │   ├── base/                    
│   │   ├── utils/                   
│   │   └── extensions/              
│   ├── data/                        # Core data layer
│   │   ├── database/
│   │   ├── datastore/               
│   │   └── network/                 
│   ├── ui/                          # Core UI
│   │   ├── theme/                   
│   │   ├── components/              
│   │   └── widgets/                 
│   └── domain/                      # Core domain
│       ├── model/                   
│       └── repository/              
│
├── feature/                         # Business feature modules
│   ├── ledger/                      # 💰 Accounting (Financial Management)
│   ├── todo/                        # ✅ Todo (Task Management)
│   ├── habit/                       # 🎯 Habits (Habit Building)
│   ├── period/                      # 🌸 Period Tracker (Women's Health) - Future
│   ├── schedule/                    # 📅 Shift Schedule (Work Management) - Future
│   └── diary/                       # 📔 Diary (Personal Records) - Future
│
├── shared/                          # Shared business modules
│   ├── user/                        
│   ├── sync/                        
│   ├── backup/                      
│   ├── notification/                
│   └── analytics/                   
│
└── build-logic/                     # Build logic
```

### Module Structure Example (Ledger Module)
```
feature-ledger/
├── api/
│   └── LedgerApi.kt                 # Public interface for other modules
├── data/
│   ├── local/
│   │   ├── dao/                     # DAOs for accounts, transactions, etc.
│   │   └── entity/                  # Database entities
│   └── repository/                  # Repository implementations
├── domain/
│   ├── model/                       # Domain models (Account, Transaction, etc.)
│   └── usecase/                     # Business logic use cases
│       ├── account/                 # Account-related use cases
│       ├── transaction/             # Transaction-related use cases
│       ├── budget/                  # Budget-related use cases
│       └── savings/                 # Savings goal use cases
└── presentation/
    ├── navigation/                  # Module-internal navigation
    ├── account/                     # Account management screens
    ├── transaction/                 # Transaction screens
    ├── statistics/                  # Statistics screens
    └── viewmodel/                   # All ViewModels
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
class HomeViewModel(
    private val ledgerApi: LedgerApi,
    private val todoApi: TodoApi,
    private val habitApi: HabitApi
) : ViewModel() {
    // Aggregate data from multiple modules
}
```

## Module Development Guidelines

### Feature Module Structure
Every feature module MUST follow this structure:
```
feature-[domain]/
├── api/           # 对外暴露的接口 (Public API)
├── data/          # 数据层实现 (Data layer)
├── domain/        # 业务逻辑 (Business logic)
└── presentation/  # UI展示 (UI layer)
```

### Development Decision Guide
When adding new functionality:

1. **判断业务领域 (Determine Business Domain)**
   - 财务相关 → `feature-ledger` module
   - 任务管理 → `feature-todo` module
   - 习惯养成 → `feature-habit` module
   - 新的生活领域 → Create new feature module

2. **模块间协作 (Inter-module Collaboration)**
   - Use Navigation Component for UI navigation
   - Share data through API interfaces
   - Use event bus for event communication

3. **数据管理 (Data Management)**
   - Each module manages its own database tables
   - Cross-module data accessed through APIs
   - Shared data placed in shared modules

### New Feature Development Checklist
- [ ] Identified the correct business domain?
- [ ] Placed in appropriate feature module?
- [ ] Following module's internal structure (api/data/domain/presentation)?
- [ ] Dependencies follow allowed directions?
- [ ] Module's public API is minimal and well-defined?
- [ ] Database entities in module's data/local/entity/?
- [ ] Inter-module communication through defined APIs only?

### Module Dependencies
- All ViewModels are `@HiltViewModel` annotated
- Repositories are `@Singleton` scoped
- Database and DAOs are provided through `DatabaseModule`
- Workers use `@HiltWorker` for dependency injection

### Architecture Migration Strategy
The project is currently transitioning from traditional layered architecture to domain-based modular architecture:
1. **Existing code remains in current structure** during transition
2. **New features should follow the modular architecture** described above
3. **Gradual migration**: When significantly modifying existing features, consider migrating them to the appropriate feature module
4. **Priority**: Ledger module is the most complex and should be properly modularized first

### Key Features Implementation

1. **Multi-Account Support**: Transactions linked to accounts via foreign keys
2. **Category System**: Hierarchical categories with parent-child relationships
3. **Sync System**: Change tracking with `ChangeLogEntity` and sync status on all entities
4. **Recurring Transactions**: Automated transaction creation via WorkManager
5. **Budget Management**: Monthly/yearly budgets with category-based tracking
6. **Savings Goals**: Goal tracking with contribution history

### Life Domain Planning
Current and future feature modules represent different life management domains:
- **Ledger (记账)**: Complete financial management including accounts, transactions, budgets, credit cards, and savings goals
- **Todo (待办)**: Task management with priorities, deadlines, and categories
- **Habit (习惯)**: Habit tracking with streaks, reminders, and statistics
- **Period (经期)**: Women's health tracking with predictions and health insights
- **Schedule (排班)**: Work shift management with calendar integration
- **Diary (日记)**: Personal journaling with mood tracking and photo attachments

Each domain should be self-contained with minimal cross-domain dependencies.

## File Organization Standards

### Document and Script Organization
To maintain a clean project structure, follow these file placement rules:

```
项目根目录/
├── doc/                    # All project documentation
│   ├── README.md          
│   ├── CHANGELOG.md       
│   ├── 架构重构设计文档.md
│   └── *.md               # Other project documents
├── scripts/                # Auxiliary scripts
│   ├── build.sh           
│   ├── release.sh         
│   └── *.sh               # Other shell scripts
├── app/                   
├── core/                  
├── feature/               
└── shared/                
```

**File Placement Rules:**
- **All .md files** must be placed in the `doc/` folder
- **All .sh scripts** must be placed in the `scripts/` folder
- Keep Android standard structure for everything else
- Do NOT scatter documents or scripts in random locations

## Build Configuration

### Key Dependencies & Versions
- Kotlin: 1.9.21
- Compose BOM: 2023.10.01
- Compose Compiler: 1.5.7
- Hilt: 2.48.1
- Room: 2.6.1
- Retrofit: 2.9.0
- WorkManager: 2.9.0
- KSP: 1.9.21-1.0.15

### Repository Configuration
The project uses Aliyun Maven mirrors for faster dependency resolution in China:
- Priority: Aliyun mirrors → Google → Maven Central
- Configuration in `settings.gradle.kts`

### Important Build Features
- ViewBinding enabled
- Compose enabled
- R8 minification for release builds
- Schema export for Room database
- KSP incremental compilation