# ADR-002: 选择Room作为本地数据库

## 状态
已采纳

## 日期
2025-06-01

## 背景
CC小记需要本地存储大量数据：
- 用户账户信息
- 待办事项
- 习惯记录
- 财务交易
- 排班信息

需要选择一个可靠、高效的本地数据库方案。

## 考虑的方案
1. **SQLite直接操作**：使用Android原生SQLite API
2. **Room**：Google官方的SQLite抽象层
3. **Realm**：第三方对象数据库
4. **ObjectBox**：高性能NoSQL数据库

## 决策
选择Room作为本地数据库解决方案。

## 原因
### 选择Room的理由
1. **官方支持**：Google官方推荐，长期维护有保障
2. **编译时验证**：SQL语句在编译时检查，减少运行时错误
3. **LiveData/Flow集成**：与Android架构组件无缝集成
4. **迁移支持**：内置数据库迁移机制
5. **类型安全**：通过注解处理器生成类型安全的代码

### 拒绝其他方案的理由
- **SQLite直接操作**：样板代码太多，容易出错
- **Realm**：学习曲线陡峭，社区支持减少
- **ObjectBox**：相对较新，生态系统不够成熟

## 实施方式
```kotlin
@Database(
    entities = [UserEntity::class, TaskEntity::class, ...],
    version = 5,
    exportSchema = true
)
abstract class CcDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    // ...
}
```

## 结果
### 正面影响
- ✅ 开发效率提高，减少样板代码
- ✅ 数据库操作更安全，编译时发现错误
- ✅ 与Coroutines完美配合，支持异步操作
- ✅ 迁移机制稳定，数据升级平滑

### 负面影响
- ❌ 需要使用注解处理器，增加编译时间
- ❌ 复杂查询的灵活性不如原生SQL
- ❌ 所有模块需要配置room-compiler

## 最佳实践
1. 每个feature模块定义自己的Entity和DAO
2. 在app模块统一注册到Database
3. 使用TypeConverter处理复杂类型
4. 导出schema用于版本控制
5. 为每个表添加同步状态字段

## 参考
- [Room官方文档](https://developer.android.com/training/data-storage/room)
- 数据库架构：`app/src/main/java/com/ccxiaoji/app/data/local/CcDatabase.kt`