# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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