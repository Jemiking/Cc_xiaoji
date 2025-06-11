# shared-notification模块迁移总结

## 迁移时间
2025-06-12

## 编译错误修复
### 问题1：CreditCardReminderWorker 依赖错误
- **错误**：`error.NonExistentClass` - NotificationManager 类无法解析
- **原因**：NotificationManager 已迁移到 shared-notification 模块
- **解决**：更新 CreditCardReminderWorker 使用 NotificationApi 接口

### 问题2：ExistingWorkPolicy 枚举值错误
- **错误**：`Unresolved reference: CANCEL_AND_REENQUEUE`
- **原因**：混淆了 ExistingWorkPolicy 和 ExistingPeriodicWorkPolicy
- **解决**：使用 ExistingWorkPolicy.KEEP 替代

## 迁移内容

### 1. 模块结构
创建了标准的三层模块结构：
- `shared/notification/api` - API接口定义
- `shared/notification/data` - 数据层实现
- `shared/notification/domain` - 领域层（预留）

### 2. API设计
定义了`NotificationApi`接口，整合了通知相关的所有功能：
- **通知发送**：任务提醒、习惯提醒、预算提醒、信用卡提醒、通用通知
- **通知调度**：使用WorkManager实现定时通知
- **通知管理**：取消通知、初始化通知渠道

### 3. 迁移的组件
- `NotificationManager` - 负责创建和发送通知
- `NotificationScheduler` - 负责调度定时通知
- `TaskReminderWorker` - 任务提醒Worker
- `HabitReminderWorker` - 习惯提醒Worker  
- `DailyCheckWorker` - 每日检查Worker

### 4. 关键改动
1. **图标修改**：使用系统图标`android.R.drawable.ic_dialog_info`代替应用图标
2. **Intent创建**：使用包名和类名创建Intent，避免对MainActivity的直接依赖
3. **桥接实现**：保留`HabitReminderSchedulerImpl`和`TodoNotificationSchedulerImpl`在app模块
4. **异步处理**：NotificationApi的所有方法都是suspend函数

### 5. 依赖更新
- app模块添加了对`shared:notification`的依赖
- MainActivity和桥接实现类更新为使用`NotificationApi`
- 删除了app模块中的旧通知代码和`NotificationModule`

## 注意事项
1. 通知图标暂时使用系统图标，后续可根据需要替换为自定义图标
2. Workers在新模块中创建时需要注意Context的获取方式
3. 桥接实现类使用`runBlocking`调用suspend函数，这是为了兼容非suspend接口

## 下一步建议
1. 考虑为不同类型的通知创建专门的图标
2. 实现DailyCheckWorker的具体逻辑（如预算检查）
3. 可以考虑添加通知配置功能，让用户自定义通知行为