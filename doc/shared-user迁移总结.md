# shared-user 模块迁移总结

## 迁移信息
- **迁移日期**: 2025-06-18
- **模块名称**: shared-user
- **迁移耗时**: 约30分钟
- **迁移人**: Claude Code

## 迁移内容

### 1. 迁移的文件
从 app 模块迁移到 shared-user 模块的文件：

#### 数据层
1. **UserEntity.kt** - 用户实体类
   - 从: `app/data/local/entity/`
   - 到: `shared/user/data/local/entity/`

2. **UserDao.kt** - 用户数据访问对象
   - 从: `app/data/local/dao/`
   - 到: `shared/user/data/local/dao/`

3. **UserRepository.kt** - 用户仓库
   - 从: `app/data/repository/`
   - 到: `shared/user/data/repository/`

4. **AuthApi.kt** - 认证API接口
   - 从: `app/data/remote/api/`
   - 到: `shared/user/data/remote/api/`

5. **AuthDto.kt** - 认证数据传输对象
   - 从: `app/data/remote/dto/`
   - 到: `shared/user/data/remote/dto/`

#### 领域层
6. **User.kt** - 用户领域模型
   - 从: `app/domain/model/`
   - 到: `shared/user/domain/model/`

### 2. 创建的新文件
1. **UserApi.kt** - 对外公开的API接口
2. **UserApiImpl.kt** - API接口实现
3. **UserModule.kt** - Hilt依赖注入模块
4. **TokenProviderImpl.kt** - TokenProvider接口的实现（在app模块）

### 3. 架构设计决策

#### 3.1 API接口设计
- 创建 `UserApi` 接口作为模块对外的唯一接口
- 隐藏内部实现细节（Repository、DAO等）
- 提供完整的用户相关功能：登录、登出、用户信息获取、令牌管理等

#### 3.2 依赖注入设计
- 使用 `UserModule` 提供内部依赖
- 使用 `UserBindModule` 绑定接口实现
- 在app模块创建 `TokenProviderImpl` 实现 `TokenProvider` 接口

#### 3.3 模块间通信
- 其他模块通过 `UserApi` 接口访问用户功能
- 避免直接依赖具体实现类

### 4. 更新的依赖

#### 4.1 更新使用方
将以下类从使用 `UserRepository` 改为使用 `UserApi`：
- `TransactionRepository`
- `SyncWorker`
- `CreditCardReminderWorker`
- `ProfileViewModel`
- `HomeViewModel`
- `LedgerViewModel`
- `BudgetViewModel`
- `RecurringTransactionViewModel`

#### 4.2 更新导入
所有引用 `UserEntity` 的实体类添加了正确的导入：
```kotlin
import com.ccxiaoji.shared.user.data.local.entity.UserEntity
```

### 5. 遇到的问题及解决方案

#### 问题1: 外键引用错误
- **原因**: 多个实体类通过外键引用UserEntity，但UserEntity已迁移
- **解决**: 批量更新所有实体类，添加UserEntity的正确导入

#### 问题2: 依赖注入错误
- **原因**: CcXiaoJiApplication仍在使用旧的UserDao导入
- **解决**: 更新导入路径为shared-user模块的路径

#### 问题3: AuthApi未找到
- **原因**: NetworkModule和RepositoryModule仍在引用已移除的AuthApi
- **解决**: 
  - 从NetworkModule移除AuthApi的provider（现在由UserModule提供）
  - 从RepositoryModule移除未使用的AuthApi导入

### 6. 验证结果
- ✅ 编译通过
- ✅ 依赖关系正确
- ✅ 模块结构符合规范
- ✅ API接口设计合理

### 7. 影响范围
- 所有需要用户认证的功能
- 所有需要获取当前用户信息的功能
- 同步功能（依赖用户令牌）
- 个人资料管理功能

### 8. 后续优化建议
1. 考虑将用户偏好设置（Preferences）也迁移到shared-user模块
2. 可以添加用户权限管理功能
3. 考虑添加多用户切换支持
4. 优化TokenProvider的实现，支持令牌刷新机制

### 9. 下一步计划
继续迁移 shared-sync 模块