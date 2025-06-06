# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

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
```

### Database Management
- Room database version: 6
- Schema location: `app/schemas/`
- When modifying database entities, increment version in `CcDatabase.kt` and create a migration in `app/src/main/java/com/ccxiaoji/app/data/local/migrations/`

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