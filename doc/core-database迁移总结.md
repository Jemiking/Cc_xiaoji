# core-database模块迁移总结

## 迁移时间
2025-06-18

## 迁移内容清单

### 1. 数据库核心类
- ✅ `CcDatabase.kt` - Room数据库主类（暂时使用PlaceholderEntity）
- ✅ `Converters.kt` - Room TypeConverters

### 2. 数据库迁移相关
- ✅ `migrations/DatabaseMigrations.kt` - 迁移管理类
- ✅ `migrations/Migration_1_2.kt` - 版本1到2的迁移
- ✅ `migrations/Migration_2_3.kt` - 版本2到3的迁移
- ✅ `migrations/Migration_3_4.kt` - 版本3到4的迁移
- ✅ `migrations/DevMigrations.kt` - 开发环境专用迁移

### 3. 其他相关类
- ✅ `DatabaseInitializer.kt` - 数据库初始化器（内容暂时注释）
- ✅ `DatabaseModuleDebugHelper.kt` - 数据库调试辅助类
- ✅ `di/CoreDatabaseModule.kt` - Hilt模块（新创建）

## 迁移过程记录

### 步骤1：创建模块目录结构
```
core/database/src/main/kotlin/com/ccxiaoji/core/database/
├── migrations/
└── di/
```

### 步骤2：迁移文件
1. 迁移Converters.kt
2. 迁移CcDatabase.kt
3. 批量迁移migrations文件夹下的所有文件
4. 迁移DatabaseInitializer.kt
5. 迁移DatabaseModuleDebugHelper.kt

### 步骤3：解决循环依赖问题
- 创建PlaceholderEntity作为占位符
- 暂时注释掉CcDatabase中的Entity和DAO引用
- 暂时注释掉DatabaseInitializer的实现
- 在app模块创建临时的CcDatabase.kt文件

### 步骤4：更新依赖配置
- 在core-database的build.gradle.kts中配置Room
- app模块保持独立的数据库创建逻辑（临时方案）

## 遇到的问题及解决方案

### 问题1：循环依赖
**原因**：CcDatabase依赖app模块的Entity和DAO，而app模块又依赖core-database
**解决**：
1. 创建PlaceholderEntity占位符
2. 注释掉CcDatabase中的Entity和DAO引用
3. 在app模块创建临时CcDatabase文件
4. 待Feature模块迁移后再恢复正常结构

### 问题2：Room编译错误
**原因**：@Database注解必须至少包含一个Entity
**解决**：创建PlaceholderEntity作为占位符

### 问题3：DatabaseInitializer依赖DAO
**原因**：DatabaseInitializer直接依赖具体的DAO实现
**解决**：暂时注释掉所有方法实现，待Feature模块迁移后恢复

## 验证结果
- ✅ 编译成功
- ⚠️ 使用临时解决方案
- ⚠️ 功能暂时不可用，需要Feature模块迁移后恢复

## 影响范围
- DatabaseModule.kt - 更新为使用app模块的临时CcDatabase
- 所有使用数据库的功能暂时保持不变

## 后续工作

### 需要恢复的内容
1. CcDatabase.kt中的Entity和DAO声明
2. DatabaseInitializer.kt的完整实现
3. 删除PlaceholderEntity
4. 删除app模块的临时CcDatabase.kt

### 恢复时机
在完成以下模块迁移后：
1. 所有Feature模块迁移完成
2. Entity和DAO迁移到各自的Feature模块
3. 解决模块间的依赖关系

## 技术债务
1. 临时的PlaceholderEntity需要删除
2. 注释的代码需要恢复
3. app模块的临时CcDatabase需要删除
4. CoreDatabaseModule的初始化回调需要优化