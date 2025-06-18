# core-common模块迁移总结

## 迁移时间
2025-06-18

## 迁移内容清单

### 1. 工具类（utils/）
- ✅ `CreditCardDateUtils.kt` - 信用卡日期计算工具类

### 2. 基础模型（model/）
- ✅ `SyncStatus.kt` - 同步状态枚举
- ✅ `CategoryType.kt` - 分类类型枚举（收入/支出）
- ✅ `RecurringFrequency.kt` - 循环频率枚举（从RecurringTransactionEntity中提取）

### 3. 常量定义（constants/）
- ✅ `DatabaseConstants.kt` - 数据库名称常量
- ✅ `DataStoreKeys.kt` - DataStore键值常量

## 迁移过程记录

### 步骤1：创建模块目录结构
```
core/common/src/main/kotlin/com/ccxiaoji/common/
├── constants/
├── model/
└── utils/
```

### 步骤2：迁移文件
1. 剪切并粘贴文件到新位置
2. 更新包名
3. 删除原文件

### 步骤3：批量更新导入语句
使用Task工具批量更新了：
- 27个文件的`SyncStatus`导入
- 5个文件的`RecurringFrequency`导入
- 3个文件的`CreditCardDateUtils`导入
- 3个文件的`DATABASE_NAME`常量引用

### 步骤4：依赖配置
- 在core-common的build.gradle.kts中添加了`datastore-preferences`依赖
- 在app模块的build.gradle.kts中添加了`implementation(project(":core:common"))`

## 遇到的问题及解决方案

### 问题1：DataStoreKeys编译错误
**原因**：core-common模块缺少datastore依赖
**解决**：在build.gradle.kts中添加`implementation("androidx.datastore:datastore-preferences:1.0.0")`

### 问题2：SyncStatus找不到引用
**原因**：部分文件（如CcXiaoJiApplication.kt）的导入没有更新
**解决**：使用Task工具批量查找并更新剩余文件

## 验证结果
- ✅ 编译成功
- ✅ 所有导入正确更新
- ✅ 功能保持不变

## 影响范围
共影响了以下类型的文件：
- Entity类：14个文件
- DAO类：5个文件
- Repository类：8个文件
- 其他类：6个文件

## 后续建议
1. core-common模块现已包含所有基础通用代码
2. 后续如有新的通用枚举、常量或工具类，应直接添加到此模块
3. 注意保持该模块的纯粹性，不要添加业务逻辑代码