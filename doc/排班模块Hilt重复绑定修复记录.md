# 排班模块Hilt重复绑定修复记录

## 修复时间
2025-06-13

## 问题描述
编译失败，Hilt报告`ScheduleRepository`被重复绑定：
- `ScheduleModule`使用`@Binds`绑定了Repository
- `ScheduleDataModule`使用`@Provides`也绑定了同一个接口
- Hilt无法决定使用哪个绑定

## 根本原因
在实现Data层时，没有检查项目中是否已存在相关的依赖注入配置，导致创建了重复的Module。

## 修复方案
采用方案一：删除重复的`ScheduleDataModule.kt`，使用已存在的`ScheduleModule`

## 具体操作
1. 删除了`/feature/schedule/data/src/.../di/ScheduleDataModule.kt`
2. 确认`ScheduleModule`已包含所需配置：
   - `@Binds`绑定`ScheduleRepositoryImpl`到`ScheduleRepository`
   - 提供`ShiftDao`和`ScheduleDao`
   - 提供`ScheduleExportHistoryDao`

## 现有依赖注入结构
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class ScheduleModule {
    @Binds
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository
    
    @Binds
    abstract fun bindScheduleApi(impl: ScheduleApiImpl): ScheduleApi
    
    companion object {
        @Provides fun provideShiftDao(database: CcDatabase): ShiftDao
        @Provides fun provideScheduleDao(database: CcDatabase): ScheduleDao
        @Provides fun provideScheduleExportHistoryDao(database: CcDatabase): ScheduleExportHistoryDao
    }
}
```

## 经验教训
1. **创建新文件前先搜索**：使用Grep工具检查是否已存在相关文件
2. **理解模块结构**：了解项目的依赖注入架构
3. **遵循Hilt最佳实践**：
   - 接口到实现的绑定使用`@Binds`
   - 创建新实例使用`@Provides`
   - 避免重复绑定

## 编译验证
```bash
./gradlew :feature:schedule:compileDebugKotlin
./gradlew :app:hiltJavaCompileDebug
```