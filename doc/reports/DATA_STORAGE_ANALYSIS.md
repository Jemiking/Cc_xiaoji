# CC小记 数据存储功能分析报告

## 📊 数据存储功能总览

### 1. 🗄️ Room数据库（主要存储方式）

#### 数据库配置
- **数据库名称**: cc_xiaoji.db
- **当前版本**: 7
- **实体数量**: 23个
- **DAO数量**: 19个
- **迁移记录**: 6次（v1→v7）

#### 核心实体分类

**用户管理**
- `UserEntity` - 用户基本信息

**任务管理**
- `TaskEntity` - 待办任务

**习惯管理**
- `HabitEntity` - 习惯定义
- `HabitRecordEntity` - 习惯记录

**记账管理**（12个实体）
- `AccountEntity` - 账户信息
- `TransactionEntity` - 交易记录
- `CategoryEntity` - 分类信息
- `BudgetEntity` - 预算管理
- `SavingsGoalEntity` - 储蓄目标
- `RecurringTransactionEntity` - 定期交易
- `CreditCardBillEntity` - 信用卡账单
- `CreditCardPaymentEntity` - 信用卡还款记录
- `TransactionTagEntity` - 交易标签
- `TagEntity` - 标签定义
- `TransactionAttachmentEntity` - 交易附件
- `AttachmentEntity` - 附件信息

**排班管理**
- `ShiftEntity` - 班次定义
- `ScheduleEntity` - 排班记录
- `PatternEntity` - 排班模式
- `ExportHistoryEntity` - 导出历史

**计划管理**
- `PlanEntity` - 计划信息
- `MilestoneEntity` - 里程碑
- `TemplateEntity` - 计划模板

**系统功能**
- `CountdownEntity` - 倒计时
- `ChangeLogEntity` - 同步日志

#### 数据库特性
- **软删除机制**: 使用`isDeleted`字段
- **同步支持**: 所有实体包含`syncStatus`字段
- **时间追踪**: `createdAt`/`updatedAt`字段
- **外键约束**: 通过`userId`关联用户
- **索引优化**: 关键查询字段建立索引
- **类型转换**: TypeConverter处理复杂类型

### 2. 💾 DataStore（轻量级键值存储）

#### 存储位置
- **文件名**: cc_xiaoji_prefs

#### 存储内容
- `current_user_id` - 当前用户ID
- `user_id` - 用户ID
- `access_token` - 访问令牌
- `refresh_token` - 刷新令牌
- `last_sync_time` - 最后同步时间
- `server_time` - 服务器时间

#### 使用场景
- 用户认证信息存储
- 应用配置存储
- 临时状态保存

### 3. 📁 文件存储

#### 备份功能
- **备份目录**: CcXiaojiBackup
- **文件格式**: cc_xiaoji_backup_[时间戳].db
- **保留策略**: 最多保留5个备份
- **备份内容**: 完整数据库文件

#### 数据导出功能
- **支持格式**: JSON, CSV, Excel
- **导出范围**: 
  - 交易记录
  - 任务列表
  - 习惯记录
  - 账户信息
  - 分类信息
  - 预算数据
  - 储蓄目标
  - 倒计时
- **导出方式**: 通过FileProvider分享

#### 排班导出功能
- **导出格式**: Excel
- **导出内容**: 排班记录
- **历史记录**: ExportHistoryEntity记录导出历史

### 4. 🔄 数据同步

#### 同步机制
- **同步状态**: `SyncStatus`枚举（SYNCED, PENDING, ERROR）
- **变更日志**: `ChangeLogEntity`记录所有数据变更
- **同步Worker**: `SyncWorker`定期执行同步
- **同步API**: `SyncService`与服务器通信

#### 同步功能
- 增量同步
- 冲突解决
- 离线支持
- 自动重试

### 5. 🔐 数据安全

#### 加密存储
- 使用`androidx.security.crypto`库
- 敏感信息加密存储（如令牌）

#### 访问控制
- 用户隔离（通过userId）
- 应用沙盒保护

### 6. 🗂️ 缓存机制

#### Repository层缓存
- Flow数据自动缓存
- 内存缓存优化查询性能

#### 图片缓存
- 交易附件本地缓存
- 用户头像缓存

### 7. 📊 数据迁移

#### 数据库迁移
- **Migration_1_2**: 添加信用卡字段
- **Migration_2_3**: 添加信用卡支付历史
- **Migration_3_4**: 添加信用卡账单管理
- **Migration_4_5**: 添加排班管理表
- **Migration_5_6**: 添加计划管理表
- **Migration_6_7**: 添加额外信用卡字段

#### 迁移策略
- 使用Room Migration API
- 保持向后兼容
- 自动执行迁移

### 8. 🎯 数据持久化最佳实践

1. **分层存储**
   - 结构化数据 → Room数据库
   - 配置数据 → DataStore
   - 大文件 → 文件系统

2. **数据完整性**
   - 事务支持
   - 外键约束
   - 级联删除

3. **性能优化**
   - 索引优化
   - 分页查询
   - 异步操作

4. **数据安全**
   - 自动备份
   - 手动导出
   - 加密存储

## 📋 总结

CC小记采用了完善的数据存储架构：
- **Room数据库**作为主要存储，管理所有业务数据
- **DataStore**存储用户偏好和认证信息
- **文件系统**用于备份和导出功能
- **同步机制**确保数据云端备份
- **安全措施**保护用户隐私

这种多层次的存储方案确保了数据的安全性、可靠性和高性能。