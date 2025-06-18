# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## ✅ Architecture Migration Completed
**This project has successfully completed the architecture migration from monolithic to modular architecture.**

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
- **Completion Date**: 2025-06-18
- **Total Progress**: 100% ✅
- **Completed Modules**: 
  - ✅ 4 Core modules (common, ui, database, network)
  - ✅ 4 Shared modules (user, sync, backup, notification)
  - ✅ 3 Feature modules (todo, habit, ledger)
- **Performance**: Compilation time reduced from 57s to 25s (56% improvement)
- **Architecture**: Clear module boundaries with API interfaces
- **Documentation**: 
  - `doc/架构迁移进度追踪.md` - Detailed progress tracking
  - `doc/架构迁移里程碑.md` - Milestone records
  - `doc/性能优化总结.md` - Performance optimization summary
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
    1. Increment version in `CcDatabase.kt`
    2. Create a migration in `app/src/main/java/com/ccxiaoji/app/data/local/migrations/`
    3. Add migration to `DatabaseMigrations.kt`
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
CC小记 (CC Xiaoji) is a **Life Management App** that integrates multiple life management modules.

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