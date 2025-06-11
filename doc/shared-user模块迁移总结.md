# shared-user模块迁移总结

## 迁移日期
2025-06-11

## 迁移背景
在第五阶段-步骤5.1中，需要创建shared-user模块，将用户相关功能从app模块迁移到独立的共享模块。

## 迁移内容

### 1. 创建的文件结构
```
shared/user/
├── build.gradle.kts                    # 模块构建配置
├── api/
│   └── src/main/kotlin/.../api/
│       └── UserApi.kt                  # 对外暴露的用户API接口
├── data/
│   └── src/main/kotlin/.../data/
│       ├── remote/
│       │   ├── api/
│       │   │   └── AuthApi.kt         # 认证API（从app模块迁移）
│       │   └── dto/
│       │       └── AuthDto.kt         # 认证DTO（从app模块迁移）
│       ├── repository/
│       │   └── UserRepository.kt      # 用户仓库实现（从app模块迁移）
│       ├── di/
│       │   └── UserModule.kt          # 依赖注入配置
│       └── UserApiImpl.kt             # UserApi实现类
└── domain/
    └── src/main/kotlin/.../domain/
        └── model/
            └── User.kt                 # 用户领域模型（从app模块迁移）
```

### 2. UserApi接口设计
```kotlin
interface UserApi {
    // 认证相关
    suspend fun login(email: String, password: String): Result<UserInfo>
    suspend fun logout()
    suspend fun refreshToken(): Result<Boolean>
    
    // 用户数据
    fun getCurrentUserFlow(): Flow<UserInfo?>
    suspend fun getCurrentUser(): UserInfo?
    suspend fun getCurrentUserId(): String
    
    // Token管理
    suspend fun getAccessToken(): String?
    suspend fun isLoggedIn(): Boolean
    
    // 同步时间管理
    suspend fun getLastSyncTime(): Long
    suspend fun updateLastSyncTime(timestamp: Long)
    suspend fun updateServerTime(timestamp: Long)
}
```

### 3. 更新的模块
- **app模块**：
  - 添加对 `shared:user` 的依赖
  - 更新 `ProfileViewModel` 使用 `UserApi`
  - 更新 `HomeViewModel` 使用 `UserApi`
  - 更新 `SyncWorker` 使用 `UserApi`
  - 更新 `CreditCardReminderWorker` 使用 `UserApi`
  - 移除 `UserRepository` 的提供
  - 移除 `AuthApi` 的提供

- **feature-ledger模块**：
  - 添加对 `shared:user` 的依赖
  - 更新 `CategoryRepository` 注入并使用 `UserApi`

### 4. 删除的文件
- `/app/.../UserRepository.kt`
- `/app/.../AuthApi.kt`
- `/app/.../AuthDto.kt`
- `/app/.../User.kt`

## 技术决策

### 1. 接口设计
- 使用 `UserInfo` 作为API层的数据模型，与内部的 `User` 领域模型分离
- 保持接口简洁，只暴露必要的功能

### 2. 依赖管理
- UserModule 负责提供 `AuthApi` 和绑定 `UserApi` 实现
- 使用 Hilt 的 `@Singleton` 确保单例

### 3. 异步处理
- 使用协程和 Flow 处理异步操作
- getCurrentUserId() 改为 suspend 函数，支持从 DataStore 读取

## 迁移效果

### 优点
1. **模块化**：用户功能独立成模块，便于维护和测试
2. **复用性**：其他模块可以通过 UserApi 访问用户功能
3. **解耦**：app模块不再直接依赖用户相关的具体实现

### 改进点
1. **类型安全**：通过接口约束，提高了类型安全性
2. **职责明确**：UserApi 专注于用户相关功能
3. **扩展性**：便于后续添加新的用户相关功能

## 后续修复的编译错误

### 1. CategoryRepository 中的 suspend 函数调用问题
- **问题**：非 suspend 函数 `createDefaultCategories` 调用了 suspend 函数 `getCurrentUserId()`
- **位置**：`feature/ledger/data/.../CategoryRepository.kt:164`
- **修复**：将 `createDefaultCategories` 改为 suspend 函数

### 2. SyncWorker 中遗漏的 userRepository 引用
- **问题**：代码仍在使用已删除的 `userRepository`
- **位置**：`app/.../SyncWorker.kt:87`
- **修复**：将 `userRepository.updateServerTime` 替换为 `userApi.updateServerTime`

## 后续工作
1. 添加单元测试
2. 实现用户信息缓存优化
3. 考虑添加用户偏好设置功能