# Core Database Module

此模块包含CC小记应用的数据库层实现，使用Room持久化库。

## 模块结构

```
core/database/
├── src/
│   ├── main/kotlin/com/ccxiaoji/core/database/
│   │   ├── CcDatabase.kt              # 主数据库类
│   │   ├── Converters.kt              # Room类型转换器
│   │   ├── dao/                       # 数据访问对象
│   │   ├── entity/                    # 数据库实体
│   │   ├── migration/                 # 数据库迁移
│   │   └── model/                     # 数据模型
│   ├── androidTest/                   # 仪器测试
│   └── test/                          # 单元测试
└── schemas/                           # 数据库schema文件
```

## 主要组件

### 数据库
- `CcDatabase`: Room数据库主类，版本号为4

### 实体 (Entities)
- `UserEntity`: 用户信息
- `AccountEntity`: 账户信息（支持信用卡账户）
- `TransactionEntity`: 交易记录
- `CategoryEntity`: 分类信息
- `BudgetEntity`: 预算设置
- `TaskEntity`: 任务/待办事项
- `HabitEntity`: 习惯追踪
- `RecurringTransactionEntity`: 定期交易
- `SavingsGoalEntity`: 储蓄目标
- 更多...

### 数据访问对象 (DAOs)
每个实体都有对应的DAO接口，提供数据库操作方法。

### 迁移 (Migrations)
- Migration_1_2: 添加sync_status字段
- Migration_2_3: 添加信用卡相关字段
- Migration_3_4: 创建信用卡账单和还款表

## 测试

### 运行单元测试
```bash
./gradlew :core:database:test
```

### 运行仪器测试
```bash
./gradlew :core:database:connectedAndroidTest
```

### 主要测试类
- `DatabaseMigrationTest`: 测试数据库迁移的正确性
- `DatabaseIntegrationTest`: 测试完整的数据库功能流程
- `UserDaoTest`: 测试UserDao的操作
- `ConvertersTest`: 测试类型转换器

## 使用方式

### 在其他模块中使用
```kotlin
dependencies {
    implementation(project(":core:database"))
}
```

### 注入数据库
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CcDatabase {
        return Room.databaseBuilder(
            context,
            CcDatabase::class.java,
            CcDatabase.DATABASE_NAME
        ).addMigrations(
            Migration_1_2,
            Migration_2_3,
            Migration_3_4
        ).build()
    }
    
    @Provides
    fun provideUserDao(database: CcDatabase): UserDao = database.userDao()
    
    // ... 其他DAO的提供方法
}
```

## 注意事项

1. **数据库版本**: 当前版本为4，任何schema更改都需要创建新的迁移
2. **类型转换**: 使用`Converters`类处理复杂类型的转换
3. **同步状态**: 所有实体都包含`syncStatus`字段用于数据同步
4. **测试覆盖**: 添加新功能时请确保有对应的测试

## 开发指南

### 添加新实体
1. 在`entity`包中创建新的实体类
2. 在`CcDatabase`中注册实体
3. 创建对应的DAO接口
4. 增加数据库版本号并创建迁移
5. 添加测试

### 修改现有实体
1. 修改实体类
2. 创建数据库迁移
3. 更新相关测试
4. 确保向后兼容性